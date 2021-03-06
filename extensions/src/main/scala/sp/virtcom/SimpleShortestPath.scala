package sp.virtcom

import akka.actor._
import sp.system._
import sp.system.messages._
import sp.domain._
import sp.domain.Logic._
import scala.concurrent.Future
import akka.util._
import akka.pattern.ask
import scala.concurrent.duration._
import sp.supremicaStuff.auxiliary.DESModelingSupport
import collection.immutable.SortedMap
import scala.math
import sp.services.sopmaker.MakeASop
import scala.util.{Success, Failure}

import org.json4s._
import scala.annotation.tailrec

object SimpleShortestPath extends SPService {
  val specification = SPAttributes(
    "service" -> SPAttributes(
      "group" -> "External",
      "description" -> "Complete all operations asap. (Variables need to end up at initial state after the ops are completed)"
    )
  )
  val transformTuple = ()
  val transformation = List()
  def props() = ServiceLauncher.props(Props(classOf[SimpleShortestPath]))
}

class SimpleShortestPath extends Actor with ServiceSupport with DESModelingSupport with MakeASop {
  implicit val timeout = Timeout(100 seconds)

  import context.dispatcher

  def receive = {
    case r@Request(service, attr, ids, reqID) => {
      val replyTo = sender()
      implicit val rnr = RequestNReply(r, replyTo)
      val progress = context.actorOf(progressHandler)
      progress ! SPAttributes("progress" -> "starting search")

      val core = r.attributes.getAs[ServiceHandlerAttributes]("core").get
      val ops = ids.filter(_.isInstanceOf[Operation]).map(_.asInstanceOf[Operation])
      val things = ids.filter(_.isInstanceOf[Thing]).map(_.asInstanceOf[Thing])

      val rl = List(SOPSpec("Result", List(dijkstra(things,ops))))
      replyTo ! Response(rl, SPAttributes(), rnr.req.service, rnr.req.reqID)
      self ! PoisonPill
      progress ! PoisonPill
    }
    case _ => sender ! SPError("Ill formed request");
  }

  // finish all ops asap
  def dijkstra(things: List[Thing], ops: List[Operation]): SOP = {
    import de.ummels.prioritymap.PriorityMap
    val opsvars = ops.map(o => o.id -> sp.domain.logic.OperationLogic.OperationState.inDomain).toMap
    val statevars = things.map(sv => sv.id -> sv.inDomain).toMap ++ opsvars
    implicit val props = EvaluateProp(statevars, Set(), ThreeStateDefinition)
    val initState = State(getIdleState(things.toSet).state ++ ops.map(_.id -> OperationState.init).toMap)
    val goalState = State(getIdleState(things.toSet).state ++ ops.map(_.id -> OperationState.finished).toMap)
    val inf = Double.PositiveInfinity
    case class Node(state: State, running: PriorityMap[Operation, Double] = PriorityMap()) {
      override def equals(o: Any) = o match {
        case rhs: Node => this.state == rhs.state
        case _ => false
      }
      override def hashCode = state.hashCode
    }
    val initNode = Node(initState)
    val goalNode = Node(goalState)

    def filterOnPreconds(ops: List[Operation], state: State) = {
      ops.filter(_.conditions.filter(_.attributes.getAs[String]("kind").getOrElse("") == "precondition").headOption
        match {
        case Some(cond) => cond.eval(state)
        case None => true
      })
    }

    // adapted from Michael Ummels
    def go(active: PriorityMap[Node,Double], res: Map[Node, Double], pred: Map[Node, Node]):
        (Map[Node, Double], Map[Node, Node]) =
      if (active.isEmpty) (res, pred)
      else if(active.head._1 == goalNode) (res + active.head, pred) // only care about the goal...
      else {
        val (node,t) = active.head
        if(res.size % 1000 == 0) {
          println("------------------------------------------------------------------------------")
          println("Searched " + res.size + " nodes, " + active.size + " open nodes, at time: " + t)
        }
        val start = filterOnPreconds(ops, node.state).map(o => {
          (Node(o.next(node.state), node.running + (o -> (t + o.attributes.getAs[Double]("duration").getOrElse(0.0)))),t)
        })
        val finish = if(node.running.nonEmpty) {
          val (op,deadline) = node.running.head
          List((Node(op.next(node.state), node.running - op),deadline))
        } else List()

        val betterNeighbors = (start++finish).filter{case (n,nt) => !res.contains(n) && nt < active.getOrElse(n, inf)}
        go(active.tail ++ betterNeighbors, res + (node -> t), pred ++ betterNeighbors.map(_._1->node))
      }

    val t0 = System.nanoTime()
    val (res,pred) = go(PriorityMap(initNode -> 0.0), Map(), Map())
    val t1 = System.nanoTime()

    def findPath(curNode: Node, pred: Map[Node,Node], acum: List[Node]): List[Node] = {
      pred.get(curNode) match {
        case Some(src) =>
          findPath(src, pred, curNode :: acum)
        case None =>
          curNode::acum
      }
    }
    val path = findPath(goalNode, pred, List())

    def getTimes(path: List[Node], olr: Set[Operation]=Set(), start: Map[ID, Double]=Map(),finish: Map[ID, Double]=Map()):
        (Map[ID, Double],Map[ID, Double]) = {
      path match {
        case x::xs =>
          val nr = x.running.map(_._1).toSet
          val s = nr.diff(olr)
          val f = olr.diff(nr)
          getTimes(xs,nr,start ++ s.map(_.id -> res(x)).toMap,finish ++ f.map(_.id -> res(x)).toMap)
        case Nil => (start,finish)
      }
    }
    val (start,finish) = getTimes(path)

    // sanity check
    ops.foreach { op =>
      val dur = op.attributes.getAs[Double]("duration").getOrElse(0.0)
      assert(Math.abs(finish(op.id) - start(op.id) - dur) < 0.000001)
    }

    def rel(op1: ID,op2: ID): SOP = {
      if(finish(op1) <= start(op2))
        Sequence(op1,op2)
      else if(finish(op2) <= start(op1))
        Sequence(op2,op1)
      else
        Parallel(op1,op2)
    }

    val pairs = (for {
      op1 <- ops
      op2 <- ops if(op1 != op2)
        } yield Set(op1.id,op2.id)).toSet

    val rels = pairs.map { x => (x -> rel(x.toList(0),x.toList(1))) }.toMap
    val sop = makeTheSop(ops.map(_.id), rels, EmptySOP)

    println("Goal state at t="+res(goalNode)+" found after " + (t1 - t0)/1E9 + "s and " + res.size + " searched nodes.")
     
    sop.head
  }
}
