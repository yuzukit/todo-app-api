package controllers.todo

import javax.inject._
import play.api.mvc._
import play.api.data.{Form, FormError}
import play.api.data.Forms._
import play.api.data.format.Formats._ 
import play.api.data.format.{Formatter, Formats}

import model.{ViewValueTodo, ViewValueList}
import lib.persistence.default.TodoRepository
import lib.persistence.default.TodoCategoryRepository
import lib.model.{Todo, TodoCategory}
import form.TodoFormData

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import play.api.i18n.I18nSupport

import ixias.util.EnumStatus

//import scala.concurrent.ExecutionContext.Implicits.global

//case class TodoFormData(category_id: Int, title: String, body: String, state: Int)

@Singleton
class TodoController @Inject()(
  val controllerComponents: ControllerComponents
  )(implicit val ec: ExecutionContext) extends BaseController 
  with I18nSupport {
  // Todo登録用のFormオブジェクト
  val form = Form(
    // html formのnameがcontentのものを140文字以下の必須文字列に設定する
    mapping(
      "category_id"   -> number,
      "title"         -> nonEmptyText(maxLength = 140),
      "body"          -> nonEmptyText(maxLength = 140),
      "state"         -> default     (number, Todo.Status.IS_UNTOUCHED.code.toInt)
    )(TodoFormData.apply)(TodoFormData.unapply)
  )

  def index() = Action async { implicit req =>
    val todoFuture     = TodoRepository.getall()
    val categoryFuture = TodoCategoryRepository.getall()
    for {
      todoSeq     <- todoFuture
      categorySeq <- categoryFuture
    } yield {
      val res = todoSeq.map(todo => ViewValueTodo(
          id            = todo.id.get,
          title         = todo.title, 
          body          = todo.body, 
          state         = todo.state, 
          category_name = categorySeq.collectFirst{case category 
            if category.id == Some(todo.category_id) => category.name}.getOrElse("not found")))

      Ok(views.html.todo.list(
        ViewValueList(
          title  = "タスク一覧",
          cssSrc = Seq("main.css"),
          jsSrc  = Seq("main.js"),
          data   = res
        ),
      ))
    }
  }

  /**
    * 登録画面の表示用
    */
  def register() = Action async { implicit request: Request[AnyContent] =>
    for {
      categorySeq <- TodoCategoryRepository.getall()
    } yield {
      val categoryRadioGroup = categorySeq.map(category => (category.id.get.toString, category.name))
      Ok(views.html.todo.store(form, categoryRadioGroup))
    }
  }

  /**
    * 登録処理実を行う
    */
  def store() = Action async { implicit request: Request[AnyContent] =>
     // foldでデータ受け取りの成功、失敗を分岐しつつ処理が行える
    form.bindFromRequest().fold(
       // 処理が失敗した場合に呼び出される関数
       (formWithErrors: Form[TodoFormData]) => {
        for {
          categorySeq <- TodoCategoryRepository.getall()
        } yield {
          val categoryRadioGroup = categorySeq.map(category => (category.id.get.toString, category.name))
          //Future.successful(BadRequest(views.html.todo.store(formWithErrors, categoryRadioGroup)))
          BadRequest(views.html.todo.store(formWithErrors, categoryRadioGroup))
        }
       },
       // 処理が成功した場合に呼び出される関数
       (todoFormData: TodoFormData) => {
         for {
           // データを登録。returnのidは不要なので捨てる
            res <- TodoRepository.add(Todo(
              //None, 
              TodoCategory.Id(todoFormData.category_id),
              todoFormData.title, 
              todoFormData.body, 
              Todo.Status(code = todoFormData.state.toShort),
              ))
         } yield {
          Redirect(routes.TodoController.index())
         }
       }
    )
  }

  /**
    * 編集画面を開く
    */
  def edit(id: Long) = Action async { implicit request: Request[AnyContent] =>
    val todoFuture     = TodoRepository.getall()
    val categoryFuture = TodoCategoryRepository.getall()
    for {
      todoSeq     <- todoFuture
      categorySeq <- categoryFuture
   } yield {
      val todoOpt = todoSeq.find(_.id == Some(Todo.Id(id)))
      val categoryRadioGroup = categorySeq.map(category => (category.id.get.toString, category.name))
      todoOpt match {
          case Some(todo: Todo) =>
            Ok(views.html.todo.edit(
            // データを識別するためのidを渡す
            Todo.Id(id),
            // fillでformに値を詰める
            form.fill(TodoFormData(
              todo.category_id.toInt,
              todo.title,
              todo.body,
              todo.state.code.toInt
            )),
            categoryRadioGroup,
          ))
          case None        =>
            NotFound(views.html.error.page404())
     }
   }
  }

  /**
    * 対象のツイートを更新する
    */
  def update(id: Long) = Action async { implicit request: Request[AnyContent] =>
    form.bindFromRequest().fold(
      (formWithErrors: Form[TodoFormData]) => {
        for {
          categorySeq <- TodoCategoryRepository.getall()
        } yield {
          val categoryRadioGroup = categorySeq.map(category => (category.id.get.toString, category.name))
          BadRequest(views.html.todo.edit(Todo.Id(id), formWithErrors, categoryRadioGroup))
        }
      },
      (data: TodoFormData) => {
        for {
          oldTodoEntity <- TodoRepository.get(Todo.Id(id))
          count         <- TodoRepository.update(
            oldTodoEntity.get.map(_.copy(
              category_id = TodoCategory.Id(data.category_id),
              title       = data.title,
              body        = data.body,
              state       = Todo.Status(code = data.state.toShort),
            ))
          )
        } yield {
          Redirect(routes.TodoController.index())
        }
      }
      )
  }

  /**
   * 対象のデータを削除する
   */
  def delete() = Action async { implicit request: Request[AnyContent] =>
    // requestから直接値を取得するサンプル
    val idOpt = request.body.asFormUrlEncoded.get("id").head
    for {
      result <- TodoRepository.remove(Todo.Id(idOpt.toLong))
    } yield {
      Redirect(routes.TodoController.index())
    }
  }
}