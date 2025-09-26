<#include "/shared/accordion.ftl">

<#macro sidebar>
  <aside class="dv-sidebar">
    <div class="dv-sidebar-header">
      <h1 class="dv-bold">Dynamic Visualizations</h1>
    </div>

    <div class="dv-sidebar-body">
      <a 
        class="dv-btn dv-menu-link"
        href="/"
      >
        <i class="bi bi-list"></i>
        <span>Menu</span>
      </a>

      <div class="dv-menu-title">Pipeline Editor</div>
      <label class="w-100">
        <span>Name</span>
        <input type="text" class="dv-text-input" value="main" />
      </label>

      <button type="button" class="dv-btn-outline w-100 my-2">
        <i class="bi bi-plus-circle"></i>
        <span>Add generator</span>
      </button>

      <@accordion icon="bi bi-grid" title="Widgets">
        <p>Add new widgets by dragging them into the grid area.</p>
        <div class="dv-widgets-container">
          <template id="new-widget-template">
            <div class="dv-new-widget">
              <div class="dv-widget-draggable" title="Drag to add">
                <i></i>
              </div>
              <span class="dv-widget-title"></span>
            </div>
          </template>
        </div>
      </@accordion>

      <button id="save-button" type="button" class="dv-btn-primary w-100 my-2">
        <i class="bi bi-floppy"></i>
        <span>Save</span>
      </button>
    </div>
  </aside>
</#macro>
