import { GridStack } from "https://cdn.jsdelivr.net/npm/gridstack@12.3.3/+esm";
import getter from "./getter.js";

function init(configs) {
  const grid = GridStack.init({
    animate: false,
    float: true,
    disableDrag: true,
    disableResize: true,
  });

  grid.load(configs);

  setUpCharts(configs);
}

function setUpCharts(configs) {
  document.querySelectorAll("[data-dv-chart]").forEach((node) => {
    const id = node.dataset.dvChart;
    const config = configs.find((conf) => conf.id === id);

    const ChartClass = getter[config.type];
    const endpoint = window.location.origin + "/api/data?id=" + id;

    const options = { ...config.options, ...getDimensions(node) };

    new ChartClass(node, endpoint, options).render();
  });
}

function getDimensions(element) {
  const area = element.querySelector(".dv-chart-area");
  const rect = area.getBoundingClientRect();

  return { width: rect.width, height: rect.height };
}

export default { init };
