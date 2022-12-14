package json.writes

import play.api.libs.json.{Json, Writes}
import lib.model.{Todo, TodoCategory}
import model.ViewValueCategory

case class JsValueCategoryListItem(
    id:    Long,
    name:  String,
    slug:  String,
    color: TodoCategory.ColorStatus
)

object JsValueCategoryListItem {
  implicit val writes: Writes[JsValueCategoryListItem] = Json.writes[JsValueCategoryListItem]
  
  def apply(categoryListItem: ViewValueCategory): JsValueCategoryListItem =
    JsValueCategoryListItem(
      id    = categoryListItem.id,
      name  = categoryListItem.name,
      slug  = categoryListItem.slug,
      color = categoryListItem.color
    )
}