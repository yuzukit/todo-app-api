package controllers.todo

import javax.inject._
import play.api.mvc._

import model.ViewValueTodo
import lib.persistence.default.TodoRepository
import lib.persistence.default.TodoCategoryRepository
import lib.model.Todo

import scala.concurrent.ExecutionContext

@Singleton
class TodoController @Inject()(val controllerComponents: ControllerComponents)(implicit val ec: ExecutionContext) extends BaseController {

  // def index() = Action { implicit req =>
  //   val vv = ViewValueTodo(
  //     title  = "タスク一覧",
  //     cssSrc = Seq("main.css"),
  //     jsSrc  = Seq("main.js"),
  //     data   = TodoRepository.getall(),
  //   )
  //   Ok(views.html.todo.list(vv))
  // }

  def index() = Action async { implicit req =>
    for {
      todoSeq     <- TodoRepository.getall()
      categorySeq <- TodoCategoryRepository.getall()
    } yield {
      val res = todoSeq.map(todo => (
          todo.title, 
          todo.body, 
          todo.state, 
          categorySeq.filter(_.id.get == todo.category_id).head.name))
          
      Ok(views.html.todo.list(ViewValueTodo(
        title  = "タスク一覧",
        cssSrc = Seq("main.css"),
        jsSrc  = Seq("main.js"),
        data   = res
      )))
    }
  }
}