package sp.optimizerService

import gnu.jel.OP
import sp.domain._
import sp.extensions._


/**
  * Created by Kristian Eide on 2016-03-09.
  */
class optimizerService extends IDAble {
/*
  case class TempState (
//    name: String,
    values: List(Contiodns) = List(),
    id: ID = id.newID()
    ) extends IDAble{
  }
  case class Transition (
    gCost: Int,
    head: tempState,
    tail: tempState,
    OPs: List(tempOP) = List(),
    id: ID = id.newID()
    ) extends IDAble{
  }
  case class Node (
    name: Int,
    state: tempState,
    in: Transition,
    out: List(Transition) = List(),
    gCost: Int,
    hCost: Int,
    fCost: int,
    id: ID = id.newID()
    ) extends IDAble {
  }

  case class TempOP(
  condisitons: List(condition) = List(),
  actions: List(abillity) = List(),
  gCost: Int,
  id: ID = id.newID()
  )  extends IDAble{
  }

  var opLIST: List(TempOP) = List(
  var runFlexlink: TeamOP = new TempOP List(condition) = List((moveOut || moveIN), List(abillity)= List(flexlink.run = true)),
  var loadBuildPallets: TeamOP = new TempOP List(condition) = List((orderBuild && operatorReady), List(abillity)= List(operatorLoading = true)),
  var loadBuildPallets: TeamOP = new TempOP List(condition) = List((orderMaterial && operatorReady), List(abillity)= List(operatorLoading = true)),
  var raiseGive: TeamOP = new TempOP List(condition) = List((), List(abillity)= List()), //ingen abiility då detta sker automatiskt då det finns pallet vid stop

  )
  */
  //psudokod för optimerings algoritme
  /*
  tempNode createInitalNode(wallSchem: array[int]){
    var j: int = 0,
    for(int i; i <wallSchem.size(); i++){
      if(0 < i){
        j = j + 5;
      }
    }                                                  //5 the time place a kub

   var newState: state new State (
    List (Conditions) = List(new Conditions),
    ID),
   var newNode: node = new Node (
    val name: Int =0,
    newTempState,
    null,
    null,
    var gCost: int = 0,
    var hCost: int = 15 + j,                           //total time for all processes
    var fCost: int = tempNode.gCost + tempNode.hCost,
    ID)
   return newTempNode
  }
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
  SOP CreateSOP (initalNode: Node ){
    var examinedNode: Node = initalNode,
    var openNodeList: List(Node) = List(examinedNode),
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
 */
}
