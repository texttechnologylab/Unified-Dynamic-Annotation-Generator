import { GridStack } from "https://cdn.jsdelivr.net/npm/gridstack@12.3.3/+esm";
import getter from "./getter.js";

function init(widgets) {
  const grid = GridStack.init({
    animate: false,
    float: true,
    disableDrag: true,
    disableResize: true,
  });

  grid.load(widgets);

  setUpCharts(widgets);
}

function setUpCharts(widgets) {
  document.querySelectorAll("[data-dv-widget]").forEach((node) => {
    const id = node.dataset.dvWidget;
    const config = widgets.find((conf) => conf.id === id);

    const ChartClass = getter[config.type];

    if (ChartClass) {
      const endpoint = window.location.origin + "/api/data?id=" + id;
      const options = { ...config.options, ...getDimensions(node) };

      new ChartClass(node, endpoint, options).render();
    } else {
      node.classList.remove("hide");
    }
  });
}

function getDimensions(element) {
  const area = element.querySelector(".dv-chart-area");
  const rect = area.getBoundingClientRect();

  return { width: rect.width, height: rect.height };
}

export default { init };
