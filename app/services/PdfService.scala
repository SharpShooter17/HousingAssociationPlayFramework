package services

import java.io.ByteArrayOutputStream

import io.github.cloudify.scala.spdf._
import javax.inject.Singleton
import model.row.BillRow

@Singleton
class PdfService {

  def createPdf(bill: BillRow): ByteArrayOutputStream = {
    val path = "C:\\Program Files\\wkhtmltopdf\\bin\\wkhtmltopdf.exe"
    val pdf = Pdf(
      executablePath = path,
      config = new PdfConfig {
        orientation := Landscape
        pageSize := "Letter"
        marginTop := "1in"
        marginBottom := "1in"
        marginLeft := "1in"
        marginRight := "1in"
      })

    val page =
      <html>
        <body>
          <h1>Bill</h1>
          <table>
            <tr>
              <td>Amount</td>
              <td>Date</td>
              <td>Type</td>
            </tr>
            <tr>
              <td>{bill.amount}</td>
              <td>{bill.date}</td>
              <td>{bill.billType}</td>
            </tr>
          </table>
        </body>
      </html>

    val outputStream = new ByteArrayOutputStream
    try {
      pdf.run(page, outputStream)
    } catch {
      case e: Exception => println(e.getMessage)
    }

    outputStream
  }

}
