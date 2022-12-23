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

import play.api.libs.json._
import json.writes.{JsValueTodoListItem, JsStateListItem}
import json.reads.{JsValueCreateTodo}

//import scala.concurrent.ExecutionContext.Implicits.global

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
    val todoFuture     = TodoRepository.getallEntity()
    val categoryFuture = TodoCategoryRepository.getallEntity()
    for {
      todoSeq     <- todoFuture
      categorySeq <- categoryFuture
    } yield {
      val res = todoSeq.map(todo => ViewValueTodo(
          id            = todo.id,
          title         = todo.v.title, 
          body          = todo.v.body, 
          state         = todo.v.state, 
          category_name = categorySeq.collectFirst{case category 
            if category.id == todo.v.category_id => category.v.name},
          color         = categorySeq.collectFirst{case category 
            if category.id == todo.v.category_id => category.v.color}
          ))

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

  def indexJson() = Action async { implicit req =>
    val todoFuture     = TodoRepository.getallEntity()
    val categoryFuture = TodoCategoryRepository.getallEntity()
    for {
      todoSeq     <- todoFuture
      categorySeq <- categoryFuture
    } yield {
      val res = todoSeq.map(todo => JsValueTodoListItem.apply(ViewValueTodo(
            id            = todo.id,
            title         = todo.v.title, 
            body          = todo.v.body, 
            state         = todo.v.state, 
            category_name = categorySeq.collectFirst{case category 
              if category.id == todo.v.category_id => category.v.name},
            color         = categorySeq.collectFirst{case category 
              if category.id == todo.v.category_id => category.v.color}
          )))
      Ok(Json.toJson(res))
    }
  }

  /**
    * 登録画面の表示用
    */
  def register() = Action async { implicit request: Request[AnyContent] =>
    for {
      categorySeq <- TodoCategoryRepository.getallEntity()
    } yield {
      val categoryRadioGroup = categorySeq.map(entity => (entity.id.toString, entity.v.name))
      Ok(views.html.todo.store(form, categoryRadioGroup))
    }
  }

  // 登録処理 api
  def save() = Action(parse.json).async { implicit req =>
    req.body
      .validate[JsValueCreateTodo]
      .fold(
        errors => {
          //Jsonパースエラーの場合のレスポンス
          Future.successful(BadRequest("error"))
        },
        todoData => {
          //Jsonパース成功時の処理
          for{
            res <- TodoRepository.add(Todo(
              TodoCategory.Id(todoData.category_id),
              todoData.title,
              todoData.body,
              Todo.Status(code = todoData.state.toShort)
            ))
          } yield {
            Ok(res.toString)
          }
        }
      )
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
          categorySeq <- TodoCategoryRepository.getallEntity()
        } yield {
          val categoryRadioGroup = categorySeq.map(entity => (entity.id.toString, entity.v.name))
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
    val todoFuture     = TodoRepository.get(Todo.Id(id))
    val categoryFuture = TodoCategoryRepository.getallEntity()
    for {
      todo        <- todoFuture
      categorySeq <- categoryFuture
   } yield {
      val categoryRadioGroup = categorySeq.map(entity => (entity.id.toString, entity.v.name))

      todo match {
          case Some(todo) =>
            Ok(views.html.todo.edit(
            // データを識別するためのidを渡す
            Todo.Id(id),
            // fillでformに値を詰める
            form.fill(TodoFormData(
              todo.v.category_id.toInt,
              todo.v.title,
              todo.v.body,
              todo.v.state.code.toInt
            )),
            categoryRadioGroup,
          ))
          case None        =>
            NotFound(views.html.error.page404())
     }
   }
  }

  def editJson(id: Long) = Action async { implicit request: Request[AnyContent] =>
    val todoFuture     = TodoRepository.get(Todo.Id(id))
    val categoryFuture = TodoCategoryRepository.getallEntity()
    for {
      todo        <- todoFuture
      categorySeq <- categoryFuture
    } yield {
      todo match {
        case Some(data) => 
          val res = JsValueTodoListItem.apply(ViewValueTodo(
            id            = Todo.Id(id),
            title         = data.v.title, 
            body          = data.v.body, 
            state         = data.v.state, 
            category_name = categorySeq.collectFirst{case category 
              if category.id == data.v.category_id => category.v.name},
            color         = categorySeq.collectFirst{case category 
              if category.id == data.v.category_id => category.v.color}
            ))
          Ok(Json.toJson(res))
        case None => BadRequest("id not found")
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
          categorySeq <- TodoCategoryRepository.getallEntity()
        } yield {
          val categoryRadioGroup = categorySeq.map(entity => (entity.id.toString, entity.v.name))
          BadRequest(views.html.todo.edit(Todo.Id(id), formWithErrors, categoryRadioGroup))
        }
      },
      (data: TodoFormData) => {
        for {
          oldTodoEntityOpt <- TodoRepository.get(Todo.Id(id))
          count            <- oldTodoEntityOpt match {
            case Some(oldTodoEntity) => TodoRepository.update(
                oldTodoEntity.map(_.copy(
                  category_id = TodoCategory.Id(data.category_id),
                  title       = data.title,
                  body        = data.body,
                  state       = Todo.Status(code = data.state.toShort),
                ))
              )
            case None => Future(NotFound(views.html.error.page404()))
          }
        } yield {
          Redirect(routes.TodoController.index())
        }
      }
      )
  }

  // 更新API
  def updateJson(id: Long) = Action(parse.json).async { implicit req =>
    req.body
       .validate[JsValueCreateTodo]
       .fold(
        errors => {
          Future.successful(BadRequest("update failure"))
        },
        todoData => {
          for {
            oldTodoEntityOpt <- TodoRepository.get(Todo.Id(id))
            count            <- oldTodoEntityOpt match {
              case Some(oldTodoEntity) => TodoRepository.update(
                  oldTodoEntity.map(_.copy(
                    category_id = TodoCategory.Id(todoData.category_id),
                    title       = todoData.title,
                    body        = todoData.body,
                    state       = Todo.Status(code = todoData.state.toShort),
                  ))
                )
              case None => Future.successful(BadRequest("update failure: id not found"))
            }
          } yield {
            Ok("update success")
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

  def deleteJson(id: Long) = Action async{ implicit req =>
    for {
      result <- TodoRepository.remove(Todo.Id(id))
    } yield {
      Redirect(routes.TodoController.index())
    }
  }

  def state() = Action async { implicit req =>
    val res = Todo.Status.values.map(state => JsStateListItem.apply(
      id = state.code,
      name = state.toString
    ))
    Future(Ok(Json.toJson(res)))
  }
}