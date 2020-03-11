package security

import controllers.routes
import model.User
import play.api.mvc._

trait Secured {
  self: AbstractController =>

  def email(request: RequestHeader): Option[String] = request.session.get("email")

  def onUnauthorized(request: RequestHeader): Result = Results.Redirect(routes.LoginController.login())

  def withAuth(f: => String => Request[AnyContent] => Result): EssentialAction = {
    Security.Authenticated[String](email, onUnauthorized) { userEmail =>
      Action(request => f(userEmail)(request))
    }
  }

  def withUser(f: User => Request[AnyContent] => Result): EssentialAction = withAuth { email =>
    implicit request =>
      Option(User(email, "pass")).map { user =>
        f(user)(request)
      }.getOrElse(onUnauthorized(request))
  }
}
