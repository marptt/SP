package sp.optimizerService


import com.jniwrapper.win32.excel.{a, b}
import sp.domain._
import akka.actor._
import sp.domain.logic._
import sp.system._
import sp.system.messages._
import sp.domain._
import sp.domain.Logic._
import scala.collection.mutable
import scala.concurrent.Future
import akka.util._
import akka.pattern.ask
import scala.concurrent._
import scala.concurrent.duration._
import sp.supremicaStuff.auxiliary.DESModelingSupport


/**
  * Created by Kristian Eide on 2016-03-09.
  */

object optimizerService extends SPService {
  val specification = SPAttributes(
    "service" -> SPAttributes(
      "group"-> "Optimizer",
      "description" -> "Makes SOPs"
    )
  )
  val transformation: List[TransformValue[_]] = List()
  def props = ServiceLauncher.props(Props(classOf[optimizerService]))
}

class optimizerService extends DESModelingSupport with Actor with ServiceSupport {

  case class Transition(gCostIn: Int, headIn: Node, tailIn: Node, OPsIn: List[Operation]) {
    var gCost: Int = gCostIn
    var head: Node = headIn
    var tail: Node = tailIn
    var OPs: List[Operation] = OPsIn
  }

  case class Node(nameIn: Int, stateIn: State, inIn: Transition, outIn: List[Transition], gCostIn: Int, hCostIn: Int, fCostIn: Int) {
    var name: Int = nameIn
    var state: State = stateIn
    var inTran: Transition = inIn
    var outTran: List[Transition] = outIn
    var gCost: Int = gCostIn
    var hCost: Int = hCostIn
    var fCost: Int = fCostIn
  }

  case class TempOP(gCostIn: Int, OPsIn: List[Operation]) {
    var gCost: Int = gCostIn
    var OPs: List[Operation] = OPsIn
  }

  def receive = {
    case r@Request(service, attr, ids, reqID) => {
      val replyTo = sender()
      implicit val rnr = RequestNReply(r, replyTo)

    def createOpsStateVars(ops: List[Operation]) = {
      ops.map(o => o.id -> sp.domain.logic.OperationLogic.OperationState.inDomain).toMap
    }
      /*
        val wallScheme: List[List[Int]] = List(List(1,2,3,0),List(1,2,3,4),List(1,2,3,4),List(1,2,3,0))

        val initOPs: Operation = null

        val statevars = things.map(sv => sv.id -> sv.inDomain).toMap ++ createOpsStateVars(initOPs)

        implicit val props = EvaluateProp(statevars, Set(), ThreeStateDefinition)

        val initState = idleState match {
          case State(map) => State(map ++ ops.map(_.id -> OperationState.init).toMap)
        }

        var initNode = Node(0, initState, null, null, 0, 0, 0)

        var tempInt: Int = 0
        var tempGCost: Int = 0
        var sizeOfWallInt: Int = 0

        for (a <- 0 to 3) {
          val currentList = wallScheme(a)
          for (b <- 0 to 3) {
            if (currentList(b) != 0) {
              tempGCost = tempGCost + 9
              initNode.state = operationMaker.listOfWallSchemeOps(tempInt).next(initNode.state)
              initNode.hCost = tempGCost
              sizeOfWallInt = sizeOfWallInt + 1
            }
            tempInt = tempInt + 1
          }
        }

        var examinedNode: Node = initNode

        var openNodeList: List [Node] = List(examinedNode)
        var closedNodeList: List [Node] = List(examinedNode)

        while(true){

          val enabledOps = ops.filter(_.conditions.filter(_.attributes.getAs[String]("kind").getOrElse("") == "precondition").headOption
          match {
            case Some(cond) => cond.eval(initState)
            case None => true
          })

          var op: Operation = enabledOps

          var transitions: List[Transition] = examinedNode.outTran
          for (each transitions: currentT){
            var newNode: node = new Node (
            val name: Int = openNodeList.getLast().name + 1,
            state,
            currentT,
            null,
            var gCost: int = currentT.gCost + examinedNode.gCost
            var hCost: int = examinedNode.hCost - currentT.cost*currentT.ops.size()
            var fCost: int = examinedNode.gCost + examinedNode.hCost
            ID)
            openNodeList.add(newNode)
            )
          }
          for(each openNodeList: currentOpenNode){
            if(currentNode != null && !openNodeList.contains(currentNode.name)){
              examinedNode = currentNode
              break
            }
            for(each openNodeList: currentOpenNode){
              if(currentOpenNode != null && !openNodeList.contains(currentOpenNode.name)){
                if(currentNode.fCost < examinedNode.fCost
                  || currentOpenNode.fCost == examinedNode.fCost && currentOpenNode.hCost < examinedNode.hCost){
                  examinedNode = currentNode
                }
              }
            }
            if(examinedNode.hCost == 0){
              break                                           //terminate the while loop
            }
          }
          val pathOfOPs = List(Operation),


          }
      */

      replyTo ! Response(List(), SPAttributes(), rnr.req.service, rnr.req.reqID)
      self ! PoisonPill

    }
  }
}


//viktig kod
/*
för att initiera-----------------------------------------
def createOpsStateVars(ops: List[Operation]) = {
    ops.map(o => o.id -> sp.domain.logic.OperationLogic.OperationState.inDomain).toMap
  }
val statevars = things.map(sv => sv.id -> sv.inDomain).toMap ++ createOpsStateVars(ops)
implicit val props = EvaluateProp(statevars, Set(), ThreeStateDefinition)

skapa initerings stats-----------------------------------
val initState = idleState match {
            case State(map) => State(map ++ ops.map(_.id -> OperationState.init).toMap)
          }
hitta möjliga vägar i grafen-----------------------------
val enabledOps = ops.filter(_.conditions.filter(_.attributes.getAs[String]("kind").getOrElse("") == "precondition").headOption
            match {
              case Some(cond) => cond.eval(initState)
              case None => true
            })

updatera state-----------------------
val newState = op.next(state)


  //psudCode for optimerings algoritme------------------------------

    SOP CreateSOP (initalNode: Node ){
    var examinedNode: Node = initalNode,
    var openNodeList: List(Node) = List(examinedNode)
    while(true){
      var closedNodeList: Liste(Node) List(examinedNode),
      examinedNode = getNodeWithTransitions(examinedNode),
      var transitions: List(transition)= examinedNode.out,
      for (each transitions: currentT){
          var newState: TempSstate = new State (
          List (Conditions) = List(new Conditions),
          ID),
        var newNode: node = new Node (
          val name: Int = openNodeList.getLast().name++,
          newTempState,
          currentT,
          null,
          var gCost :int = currentT.gCost + examinedNode.gCost,
          var hCost: int = examinedNode.hCost - currentT.cost*currentT.ops.size(),
          var fCost: int = examinedNode.gCost + examinedNode.hCost,
          ID)
        openNodeList.add(newNode)
        )
      }
      for(each openNodeList: currentOpenNode){
        if(currentNode != null && !openNodeList.contains(currentNode.name)){
         examinedNode = currentNode
          break,
      }
      for(each openNodeList: currentOpenNode){
        if(currentOpenNode != null && !openNodeList.contains(currentOpenNode.name)){
          if(currentNode.fCost < examinedNode.fCost
          || currentOpenNode.fCost == examinedNode.fCost && currentOpenNode.hCost < examinedNode.hCost){
            examinedNode = currentNode,
          }
        }
      }
      if(examinedNode.hCost == 0){
        break                                           //terminate the while loop
      }
    }
    val pathOfOPs = List(Operation),
    while (examinedNode.tail != null){

    }
  }

  tempNode createInitalNode(wallSchem: array[int]){
    var j: int = 0,
    for(int i; i <wallSchem.size(); i++){
      if(0 < i){
        j = j + 5;
      }
    }                                                  //5 the time place a kub

  Node getNodeWithTransitions(node: Node){
    var tempState: tempState = node.state,
    var transitions: List(Transitions) = List(),
    var OPs: List(TempOP) = getTempOP(tempState),
    var parallelOPs: List(TempOP) = getTempOP(tempState)
      for(each OPs: currentOP){
        if(currentOP.gCost > 0){
          parallelOPs.add(currentOP)
        }
      }
      var parallelTransition: Transtion = new Transition(
       currentOP.gCost,
       null,
       node,
       currentOP,
       ID)
      node.out.add(parallelTransition)
      for(each OPs: currentOP){
        if(currentOP.gCost == 0){
          var transition: Transtion = new Transition(
          currentOP.gCost,
          null,
          node,
          currentOP,
          ID)
        node.out.add(transition)
      }
    }
  }
 */



