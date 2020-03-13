package services

import javax.inject.{Inject, Singleton}
import model.row.UserRow
import play.api.libs.mailer.{Email, MailerClient}

class MailSenderService @Inject()(mailerClient: MailerClient) {

  def sendEmail(user: UserRow): String = {
    val email = Email(
      subject = "Account activation",
      from = "b.ujazdowski@gmail.com",
      to = Seq(user.email),
      bodyHtml = Some(
        s"""
           <html>
              <body>
                  <h3>Welcome in Our Housing Association</h3>
                  <p>
                    Please activate your account and set password until ${user.tokenExpirationDate.map(_.toString).getOrElse("")}<br />
                    <a href="http://localhost:9000/activation?token=${user.token.getOrElse("")}">Account activation</a>
                  </p>
              </body>
            </html>
        """.stripMargin)
    )
    mailerClient.send(email)
  }

}
