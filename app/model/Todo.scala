package model
import lib.model.{Todo, TodoCategory}
import lib.persistence.default.TodoRepository.EntityEmbeddedId

case class ViewValueTodo(
    id:            Todo.Id,
    title:         String,
    body:          String,
    state:         Todo.Status,
    category_name: String,
)

case class ViewValueList(
    title:  String,
    cssSrc: Seq[String],
    jsSrc:  Seq[String],
    data:   Seq[ViewValueTodo]
) extends ViewValueCommon

