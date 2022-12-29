package json.writes

import play.api.libs.json.{Json, Writes}
import lib.model.{Todo, TodoCategory}
import model.ViewValueTodo

case class JsStateListItem(
  id:   Int,
  name: String
)

object JsStateListItem {
  implicit val writes: Writes[JsStateListItem] = Json.writes[JsStateListItem]
  
  def apply(state: Todo.Status): JsStateListItem =
    JsStateListItem(
      id   = state.code,
      name = state.toString
    )
}