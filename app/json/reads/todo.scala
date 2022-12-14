package json.reads

import play.api.libs.json.{Json, Reads}

case class JsValueCreateTodo(
  category_id: Int,
  title:       String,
  body:        String,
  state:       Int
)

object JsValueCreateTodo {
  implicit val reads: Reads[JsValueCreateTodo] = Json.reads[JsValueCreateTodo]

}