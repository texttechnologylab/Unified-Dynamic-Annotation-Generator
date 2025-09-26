<#macro fileInput info accept>
  <form 
    id="file-upload" 
    action="/editor" 
    method="post" 
    enctype="multipart/form-data"
  >
    <div class="dv-file-drop-area">
      <div class="dv-drop-area-instructions">
        <i class="bi bi-cloud-arrow-up"></i>
        <div class="dv-drop-area-text">
          <span>Drag and drop file here</span>
          <span class="dv-drop-area-info">${info}</span>
        </div>
      </div>

      <label class="dv-btn-outline">
        <span>Browse files</span>
        <input
          type="file"
          name="file"
          accept="${accept}"
        />
      </label>
    </div>
  </form>
</#macro>
