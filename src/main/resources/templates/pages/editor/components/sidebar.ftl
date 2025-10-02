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
      <label class="dv-label">
        <span>Identifier:</span>
        <input id="identifier-input" type="text" class="dv-text-input" value="My Pipeline" />
      </label>

      <@accordion icon="bi bi-database" title="Sources">
        <p>Sources are the basic annotation types for the charts.</p>
        <button id="sources-button" class="dv-btn-outline" type="button">
          <span>Edit</span>
          <i class="bi bi-pencil"></i>
        </button>
      </@accordion>

      <@accordion icon="bi bi-archive" title="DerivedGenerators">
        <p>Generators are data representations for the charts. They keep record of all the data that is relevant for the final charts.</p>
        <button id="generators-button" class="dv-btn-outline" type="button">
          <span>Edit</span>
          <i class="bi bi-pencil"></i>
        </button>
      </@accordion>

      <@accordion icon="bi bi-grid" title="Widgets">
        <p>Add new widgets by dragging them into the grid area to the right.</p>
        <div class="dv-widgets-container">
          <template id="new-widget-template">
            <div class="dv-new-widget">
              <div class="dv-widget-draggable">
                <i></i>
              </div>
              <span class="dv-widget-title"></span>
            </div>
          </template>
        </div>
      </@accordion>
    </div>

    <div class="dv-sidebar-footer">
      <div class="dv-btn-group">
        <button type="button" class="dv-btn-outline" onclick="window.open('/','_self')">
          Cancel
        </button>
        <button id="save-button" type="button" class="dv-btn-primary">
          <i class="bi bi-floppy"></i>
          <span>Save</span>
        </button>
      </div>
    </div>
  </aside>
</#macro>
