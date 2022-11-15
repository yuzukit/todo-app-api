package controllers

import javax.inject._
import play.api.mvc._

import model.ViewValueTodo

@Singleton
class TodoController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  def index() = Action { implicit req =>
    val vv = ViewValueTodo(
      title  = "タスク一覧",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    Ok(views.html.todo.list(vv))
  }
}