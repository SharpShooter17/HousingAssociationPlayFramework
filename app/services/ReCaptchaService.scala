package services

import java.util.concurrent.TimeUnit

import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext}

@Singleton
class ReCaptchaService @Inject()(ws: WSClient)
                                (implicit executionContext: ExecutionContext) {
  private val URL = "https://www.google.com/recaptcha/api/siteverify"
  private val GOOGLE_KEY = "6LfJZMUUAAAAAEbKppvU1n_k4_odIlQDl53uM3Z0"

  def verify(captcha: String): Boolean = {
    val request = ws.url(URL)
    val future = request.withMethod("POST")
      .post(Map("secret" -> GOOGLE_KEY, "response" -> captcha))
      .map(response => (response.json \ "success").as[Boolean])
    Await.result(future, FiniteDuration(10, TimeUnit.SECONDS))
  }
}
