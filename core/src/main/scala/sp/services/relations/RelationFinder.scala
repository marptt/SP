package sp.services.relations

import akka.actor._
import sp.domain._

import scala.annotation.tailrec

/**
 * This message starts the identification. Returns the
 * relations identified.
 * TODO: I need to update this later for better performance. Mainly using
 * ints and arrays instead of all the objects.
 *
 * @param ops The ops should have all conditions
 *            that should be used. So add Specs
 *            before
 */
case class FindRelations(ops: List[Operation], stateVars: Map[ID, StateVariable], init: State)


class RelationFinder extends Actor with RelationFinderAlgotithms {
  def receive = {
    case FindRelations(ops, svs, init) => {

    }
  }
}

//TODO: Move these to domain.logic. RelationsLogic
trait RelationFinderAlgotithms {
  case class Setup(ops: List[Operation],
              stateVars: Map[ID, StateVariable],
              groups: List[SPAttributeValue],
              init: State,
              goal: State => Boolean)


  case class SeqResult(seq: List[Operation], goalState: State, stateMap: Map[State, IndexedSeq[Operation]])


  /**
   * Finds a random seq of operations from the initial state
   * until a goal is reached or no operations are enabled
   *
   * @param setMeUp
   * @return
   */
  def findASeq(setMeUp: Setup) = {
    val setup = prepairSetup(setMeUp)
    val ops = setup.ops
    val stateVars = setup.stateVars
    val init = setup.init
    val groups = setup.groups
    val goal = setup.goal

    import sp.domain.logic.OperationLogic._
    implicit val props = EvaluateProp(stateVars, groups.toSet)

    @tailrec
    def req(ops: IndexedSeq[Operation], s: State, seq: IndexedSeq[Operation], stateMap: Map[State, IndexedSeq[Operation]]): SeqResult = {
      val enabled = ops.filter(o => o.eval(s))
      if (enabled.isEmpty || goal(s)){
        SeqResult(seq.reverse toList, s, stateMap)
      }
      else {
        val i = scala.util.Random.nextInt(enabled.size)
        val o = enabled(i)
        val newState = o next s
        val remainingOps = ops.filterNot(_ == o)
        req(remainingOps, newState, o +: seq, stateMap + (s->enabled))
      }
    }

    req(ops.toIndexedSeq, init, IndexedSeq(), Map())
  }

  /**
   * Findes when operations are enabled. The more iterations, the better
   * @param iterations No of random sequences that are generated
   * @param opsToTest The operations that are returned in the map
   * @param setMeUp The definition for the algorithm
   * @return
   */
  def findWhenOperationsEnabled(iterations: Int, opsToTest: Set[Operation] = Set())(implicit setMeUp: Setup) = {
    val setup = prepairSetup(setMeUp)

    @tailrec
    def req(n: Int, esm: EnabledStatesMap): EnabledStatesMap  = {
      if (n <= 0) esm
      else {
        val seqRes = findASeq(setup)
        val updateMap = seqRes.stateMap.foldLeft(esm.map)((m,sm)=> {
          val checkThese = if (!opsToTest.isEmpty) sm._2 filter opsToTest.contains else sm._2
          val updatedOps = checkThese.map(o => o -> mergeAState(o, m(o), sm._1)).toMap
          m ++ updatedOps
        })
        req(n-1, EnabledStatesMap(updateMap))
      }
    }
    def mergeAState(o: Operation, e: EnabledStates, s: State) = {
      val opstate = s(o.id) // fix three State here
      val newInit = e.pre.add(s) // remove full domain for speed
      e.copy(pre = newInit)
    }


    val emptyStates = MapStates(setup.stateVars map (_._1 -> Set[SPAttributeValue]()))
    val oie = EnabledStates(emptyStates, emptyStates)
    val startMap = {if (opsToTest.isEmpty) setup.ops else opsToTest}.map(_ -> oie)
    req(iterations, EnabledStatesMap(startMap toMap))
  }


  def findOperationRelations(iterations: Int, opsToTest: Set[Operation] = Set())(implicit setMeUp: Setup) = {
    val myOps = if (opsToTest.isEmpty) setMeUp.ops.toSet else opsToTest
    val sm = findWhenOperationsEnabled(iterations, myOps)

    @tailrec
    def req(ops: List[Operation],
            map: Map[Operation, EnabledStates],
            res: Map[OperationPair, SOP] ): Map[OperationPair, SOP] = {
      ops match {
        case Nil => res
        case o1 :: rest => {
          val o1State = map(o1)
          val update = map.foldLeft(res){case (aggr, (o2, o2State)) =>
            if (o1 != o2 && !aggr.contains(OperationPair(o1.id, o2.id))) aggr + (OperationPair(o1.id, o2.id) -> matchOps(o1, o1State, o2, o2State))
            else aggr
          }
          req(rest, map, update)
        }
      }
    }

    val rels = req(myOps toList, sm.map, Map())
    RelationMap(rels, sm)
  }


  def matchOps(o1: Operation, o1State: EnabledStates, o2: Operation, o2State: EnabledStates): SOP = {
    val stateOfO2WhenO1Pre = o1State.pre(o2.id) flatMap (_.asString)
    val stateOfO1WhenO2pre = o2State.pre(o1.id) flatMap (_.asString)
    val pre = (stateOfO2WhenO1Pre, stateOfO1WhenO2pre)

    if (pre ==(Set("i"), Set("i"))) Alternative(o1, o2)
    else if (pre ==(Set("i"), Set("f"))) Sequence(o1, o2)
    else if (pre ==(Set("f"), Set("i"))) Sequence(o2, o1)
    else if (pre ==(Set("i", "f"), Set("f"))) SometimeSequence(o2, o1)
    else if (pre ==(Set("i"), Set("i", "f"))) SometimeSequence(o1, o2)
    else if (pre ==(Set("i", "f"), Set("i", "f"))) Parallel(o1, o2)
    else Other(o1, o2)
  }


  /**
   * Adds opertion statevariables to the state and the varMap
   * @param setup a setup from the user
   * @return an updated setup object
   */
  def prepairSetup(setup: Setup) = {
    if (setup.ops.isEmpty) setup
    else if (setup.stateVars.contains(setup.ops.head.id)) setup
    else {
      val opSV  = setup.ops.map(o => o.id -> StateVariable.operationVariable(o)).toMap
      val startState = setup.init.next(setup.ops.map(_.id -> StringPrimitive("i")).toMap)
      val upSV = setup.stateVars ++ opSV
      setup.copy(stateVars = upSV, init = startState)
    }

  }

  
  

}