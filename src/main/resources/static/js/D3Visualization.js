import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";

export default class D3Visualization {
  constructor(root, endpoint, margin, width, height) {
    this.root = d3.select(root);
    this.endpoint = endpoint;
    this.width = width - margin.left - margin.right;
    this.height = height - margin.top - margin.bottom;

    this.tooltip = this.root.select(".dv-tooltip");
    this.controls = this.root.select(".dv-sidepanel-body");

    // Add svg
    this.svg = this.root
      .select(".dv-chart-area")
      .append("svg")
      .attr("width", width)
      .attr("height", height)
      .append("g")
      .attr("transform", `translate(${margin.left}, ${margin.top})`);

    this.filter = {};
  }

  async fetch(params = {}) {
    const url = new URL(this.endpoint);

    for (const [key, value] of Object.entries({ ...params, ...this.filter })) {
      url.searchParams.append(key, value);
    }

    const result = await fetch(url).then((response) => response.json());

    return result;
  }

  clear() {
    this.svg.selectAll("*").remove();
  }

  render() {
    throw new Error("Method render() not implemented.");
  }

  mouseover(target) {
    this.tooltip.style("opacity", 0.9);
    d3.select(target).style("opacity", 0.8);
  }

  mousemove(top, left, html) {
    this.tooltip
      .html(html)
      .style("top", top + "px")
      .style("left", left + "px");
  }

  mouseleave(target) {
    this.tooltip.style("opacity", 0);
    d3.select(target).style("opacity", 1);
  }
}
