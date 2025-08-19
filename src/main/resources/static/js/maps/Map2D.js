import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";
import D3Visualization from "../D3Visualization.js";

export default class Map2D extends D3Visualization {
  constructor(anchor, key, { width, height }) {
    super(anchor, key, { top: 0, right: 0, bottom: 0, left: 0 }, width, height);
  }

  async render(data) {
    this.clear();

    if (!data) {
      data = await this.fetch();
    }

    // https://raw.githubusercontent.com/holtzy/D3-graph-gallery/master/DATA/world.geojson
    d3.json("/js/maps/world.geojson").then((world) => {
      // Initialize zoom functionality
      const zoom = d3
        .zoom()
        .scaleExtent([0.5, 5])
        .translateExtent([
          [0, 0],
          [this.width, this.height],
        ])
        .on("zoom", this.onZoom);
      d3.select(this.anchor).select("svg").call(zoom);

      // Map and projection
      const projection = d3
        .geoEquirectangular()
        .scale(this.width / 1.3 / Math.PI)
        .translate([this.width / 2, this.height / 2]);

      // A path generator
      const path = d3.geoPath().projection(projection);

      // Draw the map
      this.svg
        .selectAll("path")
        .data(world.features)
        .join("path")
        .attr("d", path)
        .attr("fill", "#b8b8b8")
        .style("stroke", "#fff")
        .style("stroke-width", 0.1)
        .on("mouseover", this.mouseover)
        .on("mousemove", this.mousemove)
        .on("mouseleave", this.mouseleave);

      // Draw the data
      this.svg
        .selectAll()
        .data(data)
        .join("path")
        .attr("d", path)
        .style("fill", "none")
        .style("stroke", (item) => item.color)
        .style("stroke-width", 2)
        .on("mouseover", this.mouseover)
        .on("mousemove", this.mousemove)
        .on("mouseleave", this.mouseleave);
    });
  }

  mousemove = (event, data) => {
    this.tooltip
      .html(`<strong>${data?.properties?.name || data.label}</strong>`)
      .style("left", event.pageX + 20 + "px")
      .style("top", event.pageY + "px");
  };

  onZoom = (event) => {
    this.svg.attr("transform", event.transform);
  };
}
