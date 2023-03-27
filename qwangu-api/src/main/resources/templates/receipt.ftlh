<!DOCTYPE html>
<html>

<head>
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <title>Receipt</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Caveat:wght@700&family=Inter:wght@300&display=swap"
    rel="stylesheet">
  <style>
    body {
      font-family: 'Inter', sans-serif;
      margin: 0;
      padding: 0;
      background: linear-gradient(to right, #e6e6e6, #FAF9F3);
    }

    .container {
      max-width: 600px;
      margin: 0 auto;
      padding: 30px;
      background-color: #ffffff;
      border-radius: 10px;
      box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.2);
    }

    .container .hero {
      display: block;
      margin: 0 auto;
      max-width: 100%;
      height: 20rem;
      margin-bottom: 5px;
    }

    h1,
    h2 {
      text-align: center;
    }

    .table-wrapper {
      margin: 20px 0;
      border-collapse: collapse;
      width: 100%;
    }

    .table-wrapper th,
    .table-wrapper td {
      text-align: left;
      padding: 10px;
      border-bottom: 1px solid #ddd;
    }

    .table-wrapper th {
      background-color: #f5f5f5;
    }

    .total {
      text-align: right;
      margin-top: 20px;
      font-weight: bold;
    }

    .footer {
      text-align: center;
      font-size: 14px;
      color: #666666;
      margin-top: 30px;
    }

    .social-icons {
      display: flex;
      justify-content: center;
      align-items: center;
      /* margin-top: 30px; */
      background-color: darkseagreen;
      padding-top: 0.5rem;
      padding-bottom: 0.1rem;
    }

    .social-icons a {
      display: inline-block;
      margin-right: 10px;
      color: #666666;
      font-size: 20px;
    }

    .social-icons a:hover {
      color: #4078c0;
    }

    .fa {
      transition: all 0.3s ease-in-out;
    }

    .fa:hover {
      transform: translateY(-3px);
    }

    .fa-linkedin:hover {
      color: #0077b5;
    }

    .fa-instagram:hover {
      color: #e4405f;
    }

    .fa-twitter:hover {
      color: #1da1f2;
    }

    .fa-facebook:hover {
      color: #3b5998;
    }
  </style>
</head>

<body>
  <div class="container">
    <img class="hero" src="cid:paymentImage" alt="Payment" />
    <h1 style="color: forestgreen">Receipt</h1>
    <p>Dear &{tenantName},</p>
    <p>Thank you for your payment of KES &{paidAmount}. We have received your payment and your account is now up to
      date.</p>
    <table class="table-wrapper">
      <thead>
        <tr>
          <th>Description</th>
          <th>Amount &{currency}</th>
        </tr>
      </thead>
      <tbody>
        <#list amountsEntry as entry>
            <tr>
            <td>&{entry.setName()}</td>
            <td>&{entry.getAmount()?string("0.00")}</td>
            </tr>
        </#list>
      </tbody>
    </table>
    <p class="total">Total Amount: KES &{totalAmount?string("0.00")}</p>
    <div style="text-align: center; margin-top: 20px;">
        <a href="&{signInUrl}" style="display: inline-block; background-color: darkseagreen; color: #fff; padding: 10px 20px; border-radius: 5px; text-decoration: none;">Verify Receipt</a>
    </div>
    <p>If you have any questions about your account or your payment, please don't hesitate to contact us at <a href="&{companyUrl}">&{companyName}</a>.</p>    
    <div class="footer">
      <p>Thank you for your prompt attention to this matter.</p>
      <p>Best regards,</p>
      <p><a href="&{companyUrl}">&{companyName}</a></p>
    </div>
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
</body>

</html>