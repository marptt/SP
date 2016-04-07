package sp.domain.logic

import org.json4s._
//import org.json4s.native.Serialization._

import sp.domain.{SPValue, SPAttributes}

/**
 * You should import AttributeLogic._
 * Also implict val f = formats
 *
 * Created by kristofer on 15-05-26.
 */
object AttributeLogic extends AttributeLogics {

}

trait AttributeLogics {
  import JsonLogic._
  import sp.domain.ID
  //implicit val f = jsonFormats

  implicit def strToJ(x: String): JValue = JString(x)
  implicit def intToJ(x: Int): JValue = JInt(x)
  implicit def boolToJ(x: Boolean): JValue = JBool(x)
  implicit def doubleToJ(x: Double): JValue = JDouble(x)
  implicit def idToJ(x: ID): JValue = JString(x.toString())
//  implicit def pairToSPAttr(p: (String, Any))(implicit formats : org.json4s.Formats): SPAttributes =
//    SPAttributes(p._1->Extraction.decompose(p._2))


  implicit class valueLogic(value: SPValue) {
    def to[T](implicit formats : org.json4s.Formats, mf : scala.reflect.Manifest[T]) = {
      tryWithOption(
        value.extract[T]
      )
    }
    import org.json4s.native.JsonMethods._
    def pretty = org.json4s.native.JsonMethods.pretty(render(value))
    def toJson = org.json4s.native.JsonMethods.compact(render(value))
  }

//  implicit class pairAttr(p: (String, Any))(implicit formats : org.json4s.Formats) {
//    val k = p._1
//    val v = Extraction.decompose(p._2)
//    def + : SPAttributes = {
//      val res = List(k->v)
//      SPAttributes(res)
//    }
//    def +(p2: (String, Any)): SPAttributes = {
//      val res = List(k->v, p2._1->Extraction.decompose(p2._2))
//      SPAttributes(res)
//    }
//  }

  // TODO: WE must update these names and improve the API 150916
  implicit class messLogic(x: SPAttributes) {
    val obj = x.obj
    def addTimeStamp = {
      val m = obj.filterNot(_._1 == "time") :+ ("time" -> timeStamp)
      SPAttributes(m)
    }
    def +(kv: (String, Any))(implicit formats : org.json4s.Formats) = {
      val newObj = obj.filter(_._1 != kv._1)
      SPAttributes(newObj :+ kv._1->Extraction.decompose(kv._2))
    }
    def +(xs: JObject) = {
      val keys = xs.values.keySet
      val newObj = obj.filter(x => !keys.contains(x._1))
      SPAttributes(newObj ++ xs.obj)
    }

    def dig[T](keys: String*)(implicit formats : org.json4s.Formats, mf : scala.reflect.Manifest[T]): Option[T] = {
      def req(list: List[String], obj: JObject): Option[T] = list match {
        case Nil => None
        case x :: Nil => {
          for {
            v <- obj.get(x)
            t <- tryWithOption(v.extract[T])
          } yield t
        }
        case x :: xs => {
          for {
            v <- obj.get(x) if v.isInstanceOf[JObject]
            res <- req(xs, v.asInstanceOf[JObject])
          } yield res
        }
      }
      req(keys.toList, x)
    }

    def get(key: String) = {
      val temp = if (key.nonEmpty) x \ key else x
      temp match {
        case JNothing => None
        case res: JValue => Some(res)
      }
    }

    def getAs[T](key: String)(implicit formats : org.json4s.Formats, mf : scala.reflect.Manifest[T]) = {
      for {
        x <- get(key)
        t <- tryWithOption(x.extract[T])
      } yield t
    }

    def find(key: String) = x \\ key match {
      case JObject(xs) => xs.map(_._2)
      case x: JValue => List(x)
    }

    def findAs[T](key: String)(implicit formats : org.json4s.Formats, mf : scala.reflect.Manifest[T]) = {
      for {
        x <- find(key)
        t <- tryWithOption(x.extract[T])
      } yield t
    }

    // TODO: Update the api to better handle objects in arrays KB 150907.

    def findObjects(f: List[JField] => Boolean) = {
      val t = x.filter {
        case JObject(xs) => f(xs)
        case _ => false
      }
      t.asInstanceOf[List[SPAttributes]]
    }
    def findObjectsAs[T](f: List[JField] => Boolean)(implicit formats : org.json4s.Formats, mf : scala.reflect.Manifest[T]) = {
      for {
        value <- findObjects(f)
        t <- tryWithOption(value.extract[T])
      } yield t
    }

    def findObjectsWithKeys(keys: List[String]) = {
      x.filterField {
        case JField(key, JObject(xs)) => {
          val inObj = xs.map(_._1).toSet
          keys.forall(inObj contains)
        }
        case _ => false
      }
    }
    def findObjectsWithKeysAs[T](keys: List[String])(implicit formats : org.json4s.Formats, mf : scala.reflect.Manifest[T]) = {
      for {
        value <- findObjectsWithKeys(keys)
        t <- tryWithOption(value._2.extract[T])
      } yield (value._1, t)
    }
    def findObjectsWithField(fields: List[JField]) = {
      x.filterField {
        case JField(key, JObject(xs)) => {
          fields.forall(xs contains)
        }
        case _ => false
      }
    }
    def findObjectsWithFieldAs[T](fields: List[JField])(implicit formats : org.json4s.Formats, mf : scala.reflect.Manifest[T]) = {
      for {
        value <- findObjectsWithField(fields)
        t <- tryWithOption(value._2.extract[T])
      } yield (value._1, t)
    }

    import org.json4s.native.JsonMethods._
    def pretty = org.json4s.native.JsonMethods.pretty(render(x))
    def toJson = org.json4s.native.JsonMethods.compact(render(x))
  }


  def tryWithOption[T](t: => T): Option[T] = {
    try {
      Some(t)
    } catch {
      case e: Exception => None
    }
  }
}