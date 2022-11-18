package model
//import model.ViewValueTodo
import lib.model.Todo
import lib.persistence.default.TodoRepository.EntityEmbeddedId

case class ViewValueTodo(
    title:  String,
    cssSrc: Seq[String],
    jsSrc:  Seq[String],
    data:   Option[Todo]
) extends ViewValueCommon

// case class ViewValueTodoCategory(
//     title:  String,
//     cssSrc: Seq[String],
//     jsSrc:  Seq[String],
//     data:   Option[Todo]
// )