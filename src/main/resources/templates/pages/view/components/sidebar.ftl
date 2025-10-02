<#include "/shared/accordion.ftl">
<#include "/pages/view/components/pipelineSwitcher.ftl">
<#include "/pages/view/components/checkboxSearch.ftl">
<#include "/pages/view/components/dateRange.ftl">

<#macro sidebar id pipelines>
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
      
      <div class="dv-menu-title">Pipeline</div>
      <@pipelineSwitcher pipelines=pipelines selected=id />
      
      <div class="dv-menu-title">Corpus Filter</div>
      <div class="dv-corpus-filter">
        <@accordion icon="bi bi-file-earmark-text" title="Files">
            <@checkboxSearch id="files" total=12 />
        </@accordion>

        <@accordion icon="bi bi-tags" title="Tags">
          <@checkboxSearch id="tags" total=5 />
        </@accordion>

        <@accordion icon="bi bi-calendar-week" title="Date">
            <@dateRange id="date" min="2017-04-01" max="2017-04-30" />
        </@accordion>

        <div class="dv-centered mt-2">
          <button
            id="apply-button"
            class="dv-btn-outline"
            type="button"
          >
            Apply filter
          </button>
        </div>
      </div>
    </div>
  </aside>
</#macro>
