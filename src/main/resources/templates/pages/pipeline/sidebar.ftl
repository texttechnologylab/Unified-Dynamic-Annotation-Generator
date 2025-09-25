<#include "/shared/accordion.ftl">
<#include "/pages/pipeline/filter/checkbox-search.ftl">
<#include "/pages/pipeline/filter/date-range.ftl">

<#macro sidebar pipeline filters>
  <aside class="dv-sidebar">
    <div class="dv-sidebar-header">
      <h1 class="dv-bold">Dynamic Visualizations</h1>
    </div>

    <div class="dv-sidebar-body">
      <a 
        class="dv-btn dv-menu-link"
        href="/"
      >
        <i class="bi bi-box-arrow-in-left"></i>
        <span>Pipeline selection</span>
      </a>
      
      <div class="dv-menu-title">Pipeline: ${pipeline}</div>
      <a 
        class="dv-btn dv-menu-link"
        href="/editor/${pipeline}"
      >
        <i class="bi bi-pencil"></i>
        <span>Edit configuration</span>
      </a>
      <a 
        class="dv-btn dv-menu-link"
        href="/api/visualisations?pipelineId=${pipeline}&pretty=true"
        download="pipeline.json"
      >
        <i class="bi bi-download"></i>
        <span>Export configuration</span>
      </a>
      
      <div class="dv-menu-title">Corpus Filter</div>
      <div class="dv-corpus-filter">
        <@accordion icon="bi bi-file-earmark-text" title="Files">
            <@checkboxSearch id="files" total=12 />
        </@accordion>

        <#if filters.tags??>
          <@accordion icon="bi bi-tags" title="Tags">
            <@checkboxSearch id="tags" total=5 />
          </@accordion>
        </#if>

        <#if filters.date??>
          <@accordion icon="bi bi-calendar-week" title="Date">
              <@dateRange id="date" min=filters.date.min max=filters.date.max />
          </@accordion>
        </#if>

        <div class="d-flex justify-content-center mt-2">
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
