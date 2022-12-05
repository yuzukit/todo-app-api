package controllers.category

import javax.inject._
import play.api.mvc._
import play.api.data.{Form, FormError}
import play.api.data.Forms._
import play.api.data.format.Formats._ 
import play.api.data.format.{Formatter, Formats}

import model.{ViewValueCategory, ViewValueCategoryList}
import lib.persistence.default.TodoRepository
import lib.persistence.default.TodoCategoryRepository
import lib.model.{Todo, TodoCategory}
import form.CategoryFormData

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import play.api.i18n.I18nSupport

import ixias.util.EnumStatus

//import scala.concurrent.ExecutionContext.Implicits.global

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
      "slug"  -> nonEmptyText(maxLength = 140),
      "color" -> number
    )(CategoryFormData.apply)(CategoryFormData.unapply)
  )

  def index() = Action async { implicit req =>
    for {
      categorySeq <- TodoCategoryRepository.getall()
    } yield {
      val res = categorySeq.map(category => ViewValueCategory(
        id    = category.id.get,
        name  = category.name,
        slug  = category.slug,
        color = category.color,
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

  /**
    * 編集画面を開く
    */
  def edit(id: Long) = Action async { implicit request: Request[AnyContent] =>
    for {
      categorySeq <- TodoCategoryRepository.getall()
   } yield {
      val categoryOpt = categorySeq.find(_.id == Some(TodoCategory.Id(id)))
      categoryOpt match {
          case Some(category: TodoCategory) =>
            Ok(views.html.category.edit(
            // データを識別するためのidを渡す
            TodoCategory.Id(id),
            // fillでformに値を詰める
            form.fill(CategoryFormData(
              category.name,
              category.slug,
              category.color.code.toInt,
            ))
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
      (formWithErrors: Form[CategoryFormData]) => {
        Future.successful(BadRequest(views.html.category.edit(TodoCategory.Id(id), formWithErrors)))
      },
      (data: CategoryFormData) => {
        for {
          oldEntity <- TodoCategoryRepository.get(TodoCategory.Id(id))
          count     <- TodoCategoryRepository.update(
            oldEntity.get.map(_.copy(
              name  = data.name,
              slug  = data.slug,
              color = TodoCategory.ColorStatus(code = data.color.toShort)
            ))
          )
        } yield {
          Redirect(routes.TodoCategoryController.index())
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
      result <- TodoCategoryRepository.remove(TodoCategory.Id(idOpt.toLong))
    } yield {
      Redirect(routes.TodoCategoryController.index())
    }
  }
}