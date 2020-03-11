package security

import controllers.routes
import dao.UserDAO
import model.User
import play.api.mvc._

trait Secured {
  self: AbstractController =>

  private def email(request: RequestHeader): Option[String] = request.session.get("email")

  private def onUnauthorized(request: RequestHeader): Result = Results.Redirect(routes.LoginController.login())

  private def withAuth(f: => String => Request[AnyContent] => Result): EssentialAction = {
    Security.Authenticated[String](email, onUnauthorized) { userEmail =>
      Action(request => f(userEmail)(request))
    }
  }

  def withUser(f: User => Request[AnyContent] => Result)
              (implicit userDAO: UserDAO): EssentialAction = withAuth { email =>
    implicit request =>
      userDAO.findByEmail(email).map { user =>
        f(user)(request)
      }.getOrElse(onUnauthorized(request))
  }
}
