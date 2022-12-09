package model
import lib.model.TodoCategory
import lib.persistence.default.TodoRepository.EntityEmbeddedId

case class ViewValueCategory(
    id:    TodoCategory.Id,
    name:  String,
    slug:  String,
    color: TodoCategory.ColorStatus
)

case class ViewValueCategoryList(
    title:  String,
    cssSrc: Seq[String],
    jsSrc:  Seq[String],
    data:   Seq[ViewValueCategory]
) extends ViewValueCommon