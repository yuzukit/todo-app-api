/**
  * New file.
  * 
  */

package lib.model

import ixias.model._
import ixias.util.EnumStatus

import java.time.LocalDateTime
import play.api.libs.json._

import TodoCategory._
case class TodoCategory(
  id:        Option[Id],
  name:      String,
  slug:      String,
  color:     ColorStatus,
  updatedAt: LocalDateTime = NOW,
  createdAt: LocalDateTime = NOW
) extends EntityModel[Id]

// コンパニオンオブジェクト
//~~~~~~~~~~~~~~~~~~~~~~~~
object TodoCategory {

  val  Id = the[Identity[Id]]
  type Id = Long @@ TodoCategory
  type WithNoId   = Entity.WithNoId  [Id, TodoCategory]
  type EmbeddedId = Entity.EmbeddedId[Id, TodoCategory]

  // ステータス定義
  //~~~~~~~~~~~~~~~~~
  sealed abstract class ColorStatus(val code: Short) extends EnumStatus
  object ColorStatus extends EnumStatus.Of[ColorStatus] {
    case object RED    extends ColorStatus(code = 0)
    case object BLUE   extends ColorStatus(code = 1)
    case object GREEN  extends ColorStatus(code = 2)
    case object YELLOW extends ColorStatus(code = 3)
    case object PURPLE extends ColorStatus(code = 4)

    val statusSeq = ColorStatus.values.map(state => (state.code.toString, state.toString))
    implicit val writes = Writes[ColorStatus] {
      color => Json.toJson(color.toString)
    }
  }

  // INSERT時のIDがAutoincrementのため,IDなしであることを示すオブジェクトに変換
  def apply(name: String, slug: String, color: ColorStatus): WithNoId = {
    new Entity.WithNoId(
      new TodoCategory(
        id    = None,
        name  = name,
        slug  = slug,
        color = color,
      )
    )
  }
}