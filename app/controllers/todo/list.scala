package controllers.todo

import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._

import model.ViewValueTodo
import lib.persistence.default.TodoRepository
import lib.persistence.default.TodoCategoryRepository
import lib.model.Todo

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import play.api.i18n.I18nSupport
//import scala.concurrent.ExecutionContext.Implicits.global

case class TodoFormData(category_name: String, title: String, body: String)

@Singleton
class TodoController @Inject()(
  val controllerComponents: ControllerComponents
  )(implicit val ec: ExecutionContext) extends BaseController 
  with I18nSupport {

  // Tweet登録用のFormオブジェクト
  val form = Form(
    // html formのnameがcontentのものを140文字以下の必須文字列に設定する
    mapping(
      "category_name" -> nonEmptyText,
      "title"         -> nonEmptyText(maxLength = 140),
      "body"          -> nonEmptyText(maxLength = 140)
    )(TodoFormData.apply)(TodoFormData.unapply)
  )

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

  /**
    * 登録画面の表示用
    */
  def register() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.todo.store(form))
  }

  /**
    * 登録処理実を行う
    */
  def store() = Action async { implicit request: Request[AnyContent] =>
     // foldでデータ受け取りの成功、失敗を分岐しつつ処理が行える
    form.bindFromRequest().fold(
       // 処理が失敗した場合に呼び出される関数
       (formWithErrors: Form[TodoFormData]) => {
         Future.successful(BadRequest(views.html.todo.store(formWithErrors)))
       },
       // 処理が成功した場合に呼び出される関数
       (todoFormData: TodoFormData) => {
         for {
            categorySeq <- TodoCategoryRepository.getall()
           // データを登録。returnのidは不要なので捨てる
            _ <- TodoRepository.add(Todo(
              //None, 
              categorySeq.filter(_.name == todoFormData.category_name).head.id.get,
              todoFormData.title, 
              todoFormData.body, 
              2))
         } yield {
           Redirect(routes.TodoController.index())
         }
       }
    )
  }
}