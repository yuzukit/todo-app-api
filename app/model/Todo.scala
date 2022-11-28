package model
//import model.ViewValueTodo
import lib.model.{Todo, TodoCategory}
import lib.persistence.default.TodoRepository.EntityEmbeddedId

case class ViewValueTodo(
    title:  String,
    cssSrc: Seq[String],
    jsSrc:  Seq[String],
    data:   Seq[(Todo.Id, String, String, Todo.Status, String)]
) extends ViewValueCommon

