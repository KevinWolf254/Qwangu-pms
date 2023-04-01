<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Welcome</title>
  <style>
    body {
      font-family: Arial, sans-serif;
      background-color: #f4f4f4;
      color: #444;
      font-size: 16px;
      line-height: 1.6;
      margin: 0;
      padding: 0;
    }
    .container {
      max-width: 600px;
      margin: 0 auto;
      background-color: #fff;
      padding: 20px;
    }
    .header img {
      display: block;
      margin: 0 auto;
    }
    h1 {
      font-size: 24px;
      font-weight: bold;
      margin-top: 0;
    }
    p {
      margin-bottom: 20px;
    }
    .footer {
      font-size: 12px;
      text-align: center;
      margin-top: 20px;
      color: #888;
    }
    .social-media {
      background-color: black;
    }
  </style>
</head>
<body>
    <div class="container">
      <div class="header">
        <img src="cid:welcomeImage" alt="Welcome" width="80%" height="auto">
      </div>
      <h1>Welcome</h1>
      <p>Dear &{tenantName},</p>
      <p>I hope this message finds you well. I wanted to take a moment to thank you for your prompt payment for the apartment rental. It is greatly appreciated and I look forward to having you as a tenant.</p>
      <p>I wanted to inform you that your apartment is ready for occupancy and you may move in at your convenience. As per our agreement, your move-in date is &{startDate}, and I will ensure that everything is in order before you arrive.</p>
      <p>Please let me know if there are any concerns or questions that you may have regarding the apartment or the move-in process. I am always available to assist in any way possible.</p>
      <p>Once again, thank you for choosing to rent with us. I look forward to a positive landlord-tenant relationship.</p>
      
    <div style="text-align: center; margin-top: 20px;">
      <a href="&{signInUrl}"
        style="display: inline-block; background-color: darkseagreen; color: #fff; padding: 10px 20px; border-radius: 5px; text-decoration: none;">
        Sign In
      </a>
    </div>
      <div class="footer">
        <p>Best regards,</p>
        <p><a href="&{companyUrl}">&{companyName}</a></p>
        <div class="social-media">
          <a href="&{linkedInUrl}" target="_blank"><img alt="LinkedIn" height="32" src="cid:linkedInImage"
              title="LinkedIn" width="32" /></a>
          <a href="&{instagramUrl}" target="_blank"><img alt="Instagram" height="32"
              src="cid:instagramImage" title="Instagram" width="32" /></a>
          <a href="&{twitterUrl}" target="_blank"><img alt="Twitter" height="32" src="cid:twitterImage"
              title="Twitter" width="32" /></a>
          <a href="&{facebookUrl}" target="_blank"><img alt="Facebook" height="32" src="cid:facebookImage"
              title="Facebook" width="32" /></a>
        </div>
      </div>
    </div>
  </body>
</html>