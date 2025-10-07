import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";
import { corpusFilter } from "../pages/view/filter/CorpusFilter.js";

export default class D3Visualization {
  constructor(root, endpoint, margin, width, height) {
    this.root = d3.select(root);
    this.endpoint = endpoint;
    this.width = width - margin.left - margin.right;
    this.height = height - margin.top - margin.bottom;

    this.filter = {};

    this.tooltip = d3.select(".dv-chart-tooltip");

    // Add svg
    this.svg = this.root
      .select(".dv-chart-area")
      .append("svg")
      .attr("width", width)
      .attr("height", height);
    this.svg
      .append("g")
      .attr("transform", `translate(${margin.left}, ${margin.top})`);

    // Show chart
    this.root.classed("hide", false);
  }

  async fetch() {
    const url = new URL(this.endpoint);
    const options = {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        corpus: corpusFilter.filter,
        chart: this.filter,
      }),
    };

    return await fetch(url, options).then((response) => response.json());
  }

  clear() {
    this.svg.select("g").selectAll("*").remove();
  }

  init() {
    throw new Error("Method init() not implemented.");
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
