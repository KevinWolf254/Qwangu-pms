<!DOCTYPE html>
<html>
  <head>
    <title>Invoice</title>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <style>
      * {
        box-sizing: border-box;
        font-family: Arial, sans-serif;
      }
      body {
        margin: 0;
        padding: 0;
        background-color: #f6f6f6;
      }
      .container {
        max-width: 600px;
        margin: 0 auto;
        padding: 20px;
        background-color: #ffffff;
        border-radius: 10px;
        box-shadow: 0px 2px 10px rgba(0, 0, 0, 0.1);
      }
      .header {
        text-align: center;
        margin-bottom: 20px;
      }
      .header img {
        max-width: 100%;
        height: 20rem;
        margin-bottom: 5px;
      }
      .invoice-details {
        margin-bottom: 20px;
      }
      table {
        width: 100%;
        border-collapse: collapse;
        border-spacing: 0;
        margin-bottom: 20px;
      }
      table td,
      table th {
        padding: 10px;
        border: 1px solid #dddddd;
        text-align: left;
      }
      table th {
        background-color: #f2f2f2;
      }
      .footer {
        text-align: center;
      }
    </style>
  </head>
  <body>
    <div class="container">
      <div class="header">
        <img src="cid:invoiceImage" alt="Invoice" />
        <h1>Invoice: &{startDate} to &{endDate}</h1>
      </div>
      <div class="invoice-details">
        <p>Dear &{tenantName},</p>
        <p>This message serves as a reminder for your monthly rent, which is due on &{dueDate}. Below, you will find your rent invoice for the month of &{currentMonth}, with the details of your rental charges.</p>
        <p>Please take a moment to review the invoice and ensure that all details are correct. If you have any questions or concerns, please don't hesitate to reach out to me. You can reply to this email, call or text me.</p>
        <table>
          <thead>
            <tr>
              <th>Description</th>
              <th>Amount &{currency}</th>
            </tr>
          </thead>
          <tbody>
            <#list amountsEntry as entry>
                <tr>
                <td>&{entry.getName()}</td>
                <td>&{entry.getAmount()?string("0.00")}</td>
                </tr>
            </#list>
            <tr>
                <td>Total Amount Due</td>
                <td>&{totalAmount?string("0.00")}</td>
            </tr>
          </tbody>
        </table>
        <div style="text-align: center; margin-top: 20px;">
          <a href="&{signInUrl}" style="display: inline-block; background-color: #007bff; color: #fff; padding: 10px 20px; border-radius: 5px; text-decoration: none;">Verify Invoice</a>
        </div>
        <p>Please make your payment on or before the due date mentioned in the invoice. Late payments will incur late fees.</p>
      </div>
      <div class="footer">
        <p>Thank you for your prompt attention to this matter.</p>
        <p>Best regards,</p>
        <p><a href="&{companyUrl}">&{companyName}</a></p>
        
        <div class="social-icons">
            <a href="&{linkedInUrl}" target="_blank"><img alt="LinkedIn" height="32"
                src="cid:linkedInImage" title="LinkedIn" width="32" /></a>
            <a href="&{instagramUrl}" target="_blank"><img alt="Instagram" height="32"
                src="cid:instagramImage" title="Instagram" width="32" /></a>
            <a href="&{twitterUrl}" target="_blank"><img alt="Twitter" height="32"
                src="cid:twitterImage" title="Twitter" width="32" /></a>
            <a href="&{facebookUrl}" target="_blank"><img alt="Facebook" height="32"
                src="cid:facebookImage" title="Facebook" width="32" /></a>
        </div>
      </div>
    </div>
   </body>
</html>

