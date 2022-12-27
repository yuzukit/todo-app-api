package json.writes

import play.api.libs.json.{Json, Writes}
import lib.model.{Todo, TodoCategory}
import model.ViewValueCategory
import form.CategoryFormData

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

case class JsCategoryUpdateItem(
  name:  String,
  slug:  String,
  color: Int,
)

object JsCategoryUpdateItem {
  implicit val writes: Writes[JsCategoryUpdateItem] = Json.writes[JsCategoryUpdateItem]

  def apply(categoryUpdateItem: CategoryFormData): JsCategoryUpdateItem =
    JsCategoryUpdateItem(
      name  = categoryUpdateItem.name,
      slug  = categoryUpdateItem.slug,
      color = categoryUpdateItem.color
    )
}