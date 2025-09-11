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
    <div class="container text-center">
      <div class="p-5">
        <h2 class="display-1 fw-bold p-5">Dynamic Visualizations</h2>
        <div class="text-secondary fs-3">
          Create dynamic visualizations for your own NLP annotations.
          <br />
          Simply upload a json configuration.
        </div>
      </div>

      <form action="/submit" method="post" enctype="multipart/form-data">
        <div class="d-flex justify-content-center">
          <div class="file-drop-area">
            <i class="bi bi-upload"></i>
            <span class="text-truncate"> Drag & Drop File here </span>
            <span class="m-1">or</span>
            <label class="btn btn-sm btn-outline-danger">
              Browse Files
              <input
                class="file-input"
                type="file"
                name="file"
                accept="application/json"
              />
            </label>
          </div>
        </div>

        <div class="d-flex justify-content-center">
          <div class="submit-container d-none">
            <div class="d-flex align-items-center">
              <button class="btn btn-danger" type="submit" title="Upload File">
                Upload
              </button>
              <i class="bi bi-filetype-json mx-2"></i>
              <span></span>
            </div>
            <button
              class="btn btn-sm btn-outline-danger"
              type="button"
              title="Remove File"
            >
              <i class="bi bi-trash"></i>
            </button>
          </div>
        </div>
      </form>
    </div>

    <script type="module">
      const dropArea = document.querySelector(".file-drop-area");
      const fileInput = document.querySelector(".file-input");
      const submitContainer = document.querySelector(".submit-container");
      const removeButton = submitContainer.querySelector(".btn-outline-danger");

      removeButton.addEventListener("click", () => {
        fileInput.value = "";
        submitContainer.classList.add("d-none");
      });

      // Klick auf Drop-Area öffnet File-Dialog
      dropArea.addEventListener("click", () => fileInput.click());

      // Wenn Datei im File-Dialog ausgewählt wird
      fileInput.addEventListener("change", () => {
        if (fileInput.files.length > 0) {
          submitContainer.querySelector("span").textContent =
            fileInput.files[0].name;
          submitContainer.classList.remove("d-none");
        } else {
          submitContainer.classList.add("d-none");
        }
      });

      // Drag & Drop Events
      ["dragenter", "dragover"].forEach((event) => {
        dropArea.addEventListener(event, (e) => {
          e.preventDefault();
          e.stopPropagation();
          dropArea.classList.add("dragover");
        });
      });

      ["dragleave", "drop"].forEach((event) => {
        dropArea.addEventListener(event, (e) => {
          e.preventDefault();
          e.stopPropagation();
          dropArea.classList.remove("dragover");
        });
      });

      // Datei ins Feld ziehen
      dropArea.addEventListener("drop", (e) => {
        if (e.dataTransfer.files.length > 0) {
          fileInput.files = e.dataTransfer.files;
          submitContainer.querySelector("span").textContent =
            e.dataTransfer.files[0].name;
          submitContainer.classList.remove("d-none");
        } else {
          submitContainer.classList.add("d-none");
        }
      });
    </script>
  </body>
</html>

<style>
  .file-drop-area {
    margin-top: 3rem;
    padding: 1.5rem;
    border-radius: 0.5rem;
    border: 2px dashed #000;
    background-color: #f8f9fa;
    color: #000;
    width: 50%;
    transition: all 0.2s ease;
    box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;

    i {
      font-size: 2rem;
    }
  }

  .file-drop-area.dragover {
    border-color: #3b82f6;
    background: #f0f8ff;
    color: #555;
  }

  .file-input {
    display: none;
  }

  .submit-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 50%;
    transition: all 0.2s ease;
    margin-top: 1rem;
    margin-bottom: 3rem;
    font-size: 1.1rem;
  }
</style>
