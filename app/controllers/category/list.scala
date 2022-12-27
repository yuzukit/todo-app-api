package controllers.category

import javax.inject._
import play.api.mvc._
import play.api.data.{Form, FormError}
import play.api.data.Forms._
import play.api.data.format.Formats._ 
import play.api.data.format.{Formatter, Formats}
import play.api.data.validation._

import model.{ViewValueCategory, ViewValueCategoryList}
import lib.persistence.default.TodoRepository
import lib.persistence.default.TodoCategoryRepository
import lib.model.{Todo, TodoCategory}
import form.CategoryFormData

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import play.api.i18n.I18nSupport

import ixias.util.EnumStatus

import play.api.libs.json._
import json.writes.{JsValueCategoryListItem, JsColorListItem, JsCategoryUpdateItem}
import json.reads.JsValueCreateCategory

@Singleton
class TodoCategoryController @Inject()(
  val controllerComponents: ControllerComponents
  )(implicit val ec: ExecutionContext) extends BaseController 
  with I18nSupport {
  // Todo登録用のFormオブジェクト
  val form = Form(
    // html formのnameがcontentのものを140文字以下の必須文字列に設定する
    mapping(
      "name"  -> nonEmptyText(maxLength = 140),
      "slug"  -> text.verifying(Constraints.pattern("""[0-9a-zA-Z.+]+""".r, error = "Alphanumeric input is required.")),
      "color" -> number
    )(CategoryFormData.apply)(CategoryFormData.unapply)
  )

  def index() = Action async { implicit req =>
    for {
      categorySeq <- TodoCategoryRepository.getallEntity()
    } yield {
      val res = categorySeq.map(category => ViewValueCategory(
        id    = category.id,
        name  = category.v.name,
        slug  = category.v.slug,
        color = category.v.color,
      ))

      Ok(views.html.category.list(
        ViewValueCategoryList(
          title  = "カテゴリー一覧",
          cssSrc = Seq("main.css"),
          jsSrc  = Seq("main.js"),
          data   = res
        ),
      ))
    }
  }

  def indexJson() = Action async { implicit req =>
    for {
      categorySeq <- TodoCategoryRepository.getallEntity()
    } yield {
      val res = categorySeq.map(category => JsValueCategoryListItem.apply(ViewValueCategory(
        id    = category.id,
        name  = category.v.name,
        slug  = category.v.slug,
        color = category.v.color,
      )))
      Ok(Json.toJson(res))
    }
  }
  /**
    * 登録画面の表示用
    */
  def register() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.category.store(form))
  }

  /**
    * 登録処理実を行う
    */
  def store() = Action async { implicit request: Request[AnyContent] =>
     // foldでデータ受け取りの成功、失敗を分岐しつつ処理が行える
    form.bindFromRequest().fold(
       // 処理が失敗した場合に呼び出される関数
       (formWithErrors: Form[CategoryFormData]) => {
         Future.successful(BadRequest(views.html.category.store(formWithErrors)))
       },
       // 処理が成功した場合に呼び出される関数
       (categoryFormData: CategoryFormData) => {
         for {
           // データを登録。returnのidは不要なので捨てる
            res <- TodoCategoryRepository.add(TodoCategory( 
              categoryFormData.name, 
              categoryFormData.slug, 
              TodoCategory.ColorStatus(code = categoryFormData.color.toShort),
              ))
         } yield {
          Redirect(routes.TodoCategoryController.index())
         }
       }
    )
  }

  // 登録処理 api
  def save() = Action(parse.json).async { implicit req =>
    req.body
      .validate[JsValueCreateCategory]
      .fold(
        errors => {
          //Jsonパースエラーの場合のレスポンス
          Future.successful(BadRequest("store failure"))
        },
        categoryData => {
          //Jsonパース成功時の処理
          for{
            res <- TodoCategoryRepository.add(TodoCategory(
              categoryData.name,
              categoryData.slug,
              TodoCategory.ColorStatus(code = categoryData.color.toShort)
            ))
          } yield {
            Ok(res.toString)
          }
        }
      )
  }

  /**
    * 編集画面を開く
    */
  def edit(id: Long) = Action async { implicit request: Request[AnyContent] =>
    for {
      category <- TodoCategoryRepository.get(TodoCategory.Id(id))
   } yield {
      category match {
          case Some(category) =>
            Ok(views.html.category.edit(
            // データを識別するためのidを渡す
            TodoCategory.Id(id),
            // fillでformに値を詰める
            form.fill(CategoryFormData(
              category.v.name,
              category.v.slug,
              category.v.color.code.toInt,
            ))
          ))
          case None        =>
            NotFound(views.html.error.page404())
     }
   }
  }

  def editJson(id: Long) = Action async { implicit request: Request[AnyContent] =>
    for {
      category <- TodoCategoryRepository.get(TodoCategory.Id(id))
    } yield {
      category match {
        case Some(category) => 
          val res = JsCategoryUpdateItem.apply(CategoryFormData(
            name  = category.v.name,
            slug  = category.v.slug,
            color = category.v.color.code
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
      (formWithErrors: Form[CategoryFormData]) => {
        Future.successful(BadRequest(views.html.category.edit(TodoCategory.Id(id), formWithErrors)))
      },
      (data: CategoryFormData) => {
        for {
          oldEntity <- TodoCategoryRepository.get(TodoCategory.Id(id))
          count <- oldEntity match {
            case Some(entity) => TodoCategoryRepository.update(
              entity.map(_.copy(
                name  = data.name,
                slug  = data.slug,
                color = TodoCategory.ColorStatus(code = data.color.toShort)
              ))
            )
            case None         => Future(NotFound(views.html.error.page404()))
          }
        } yield {
          Redirect(routes.TodoCategoryController.index())
        }
      }
      )
  }

  // 更新API
  def updateJson(id: Long) = Action(parse.json).async { implicit req =>
    req.body
       .validate[JsValueCreateCategory]
       .fold(
        errors => {
          Future.successful(BadRequest("update failure"))
        },
        categoryData => {
          for {
            oldEntity <- TodoCategoryRepository.get(TodoCategory.Id(id))
            result <- oldEntity match {
              case Some(entity) => TodoCategoryRepository.update(
                entity.map(_.copy(
                  name  = categoryData.name,
                  slug  = categoryData.slug,
                  color = TodoCategory.ColorStatus(code = categoryData.color.toShort)
                ))
              )
              case None         => Future.successful(None)
            }
          } yield {
            result match {
              case None    => BadRequest("update failure: id not found")
              case Some(_) => Ok(Json.toJson("update success"))
            }
          }
        }
       )
      }

  /**
   * 対象のデータを削除する
   */
  def delete() = Action async { implicit request: Request[AnyContent] =>
    // requestから直接値を取得するサンプル
    val idOpt            = request.body.asFormUrlEncoded.get("id").headOption
    idOpt match {
      case None => Future(NotFound(views.html.error.page404()))
      case Some(id) => {
        val removeFuture  = TodoCategoryRepository.remove(TodoCategory.Id(id.toLong))
        val getTodoFuture = TodoRepository.getEntitiesByCategoryId(TodoCategory.Id(id.toLong))
        for {
          removeResult <- removeFuture
          getTodo      <- getTodoFuture
          removeTodo   <- Future.sequence(getTodo.map(todo => TodoRepository.remove(todo.id)))
        } yield {
          Redirect(routes.TodoCategoryController.index())
        }
      }
    }
  }

  def deleteJson(id: Long) = Action async { implicit req =>
    for {
      res <- TodoCategoryRepository.remove(TodoCategory.Id(id))
    } yield {
      res match {
        case Some(s) => Ok(Json.toJson(s.toString))
        case None    => BadRequest("delete failure")
      }
    }
  }

  def color() = Action async { implicit req =>
    val res = TodoCategory.ColorStatus.values.map(color => JsColorListItem.apply(
      id = color.code,
      name = color.toString
    ))
    Future(Ok(Json.toJson(res)))
  }
}