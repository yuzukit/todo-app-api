package json.writes

import play.api.libs.json.{Json, Writes}
import lib.model.{Todo, TodoCategory}
import model.ViewValueTodo
import java.util.Locale.Category

case class JsColorListItem(
  id:   Int,
  name: String
)

object JsColorListItem {
  implicit val writes: Writes[JsColorListItem] = Json.writes[JsColorListItem]
  
  def apply(color: TodoCategory.ColorStatus): JsColorListItem =
    JsColorListItem(
      id   = color.code,
      name = color.toString
    )
}