package json.writes

import play.api.libs.json.{Json, Writes}
import lib.model.{Todo, TodoCategory}
import model.ViewValueTodo
import form.TodoFormData

case class JsValueTodoListItem(
  id:            Long,
  title:         String,
  body:          String,
  state:         Todo.Status,
  category_name: Option[String],
  color:         Option[TodoCategory.ColorStatus]
)

object JsValueTodoListItem {
  implicit val writes: Writes[JsValueTodoListItem] = Json.writes[JsValueTodoListItem]
  
  def apply(todoListItem: ViewValueTodo): JsValueTodoListItem =
    JsValueTodoListItem(
      id            = todoListItem.id,
      title         = todoListItem.title,
      body          = todoListItem.body,
      state         = todoListItem.state,
      category_name = todoListItem.category_name,
      color         = todoListItem.color
    )
}

case class JsTodoUpdateItem(
  category_id:   Int,
  title:         String,
  body:          String,
  state:         Int,
)

object JsTodoUpdateItem {
  implicit val writes: Writes[JsTodoUpdateItem] = Json.writes[JsTodoUpdateItem]
  
  def apply(todoUpdateItem: TodoFormData): JsTodoUpdateItem =
    JsTodoUpdateItem(
      category_id   = todoUpdateItem.category_id,
      title         = todoUpdateItem.title,
      body          = todoUpdateItem.body,
      state         = todoUpdateItem.state,
    )
}