<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>${title}</title>

    <link rel="stylesheet" href="/css/variables.css" />
    <link rel="stylesheet" href="/css/global.css" />
    <link
      rel="stylesheet"
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.7/dist/css/bootstrap.min.css"
    />
    <link
      rel="stylesheet"
      href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.13.1/font/bootstrap-icons.min.css"
    />
  </head>

  <body>
    <div class="w-50">
      <div class="dv-title">Editor</div>
      <textarea class="form-control" rows="30">
        ${json}
      </textarea>
    </div>
  </body>
</html>
