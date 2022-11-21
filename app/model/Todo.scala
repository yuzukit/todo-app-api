package model
//import model.ViewValueTodo
import lib.model.{Todo, TodoCategory}
import lib.persistence.default.TodoRepository.EntityEmbeddedId

case class ViewValueTodo(
    title:  String,
    cssSrc: Seq[String],
    jsSrc:  Seq[String],
    data:   Seq[(String, String, Int, String)]
) extends ViewValueCommon

// case class ViewValueTodoCategory(
//     title:  String,
//     cssSrc: Seq[String],
//     jsSrc:  Seq[String],
//     data:   Option[Todo]
// )