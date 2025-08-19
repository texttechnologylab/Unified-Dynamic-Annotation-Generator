import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";
import D3Visualization from "../D3Visualization.js";

export default class Network2D extends D3Visualization {
  constructor(anchor, key, { width, height, radius }) {
    super(
      anchor,
      key,
      { top: 10, right: 30, bottom: 30, left: 40 },
      width,
      height
    );

    this.radius = radius;
  }

  async render(data) {
    this.clear();

    if (!data) {
      data = await this.fetch();
    }

    // Initialize zoom functionality
    const zoom = d3
      .zoom()
      .scaleExtent([0.5, 3])
      .translateExtent([
        [0, 0],
        [this.width, this.height],
      ])
      .on("zoom", this.onZoom);
    d3.select(this.anchor).select("svg").call(zoom);

    // Initialize the links
    const link = this.svg
      .selectAll("line")
      .data(data.links)
      .join("line")
      .style("stroke", (item) => item.color);

    // Initialize the nodes
    const node = this.svg
      .selectAll("circle")
      .data(data.nodes)
      .join("circle")
      .attr("r", this.radius)
      .style("fill", (item) => item.color)
      .on("mouseover", this.mouseover)
      .on("mousemove", this.mousemove)
      .on("mouseleave", this.mouseleave);

    // This function is run at each iteration of the force algorithm, updating the nodes position.
    const onTick = () => {
      link
        .attr("x1", (item) => item.source.x)
        .attr("y1", (item) => item.source.y)
        .attr("x2", (item) => item.target.x)
        .attr("y2", (item) => item.target.y);

      node.attr("cx", (item) => item.x).attr("cy", (item) => item.y);
    };

    d3.forceSimulation(data.nodes)
      // links between nodes
      .force(
        "link",
        d3.forceLink(data.links).id((item) => item.id)
      )
      // avoid node overlaps
      .force("collide", d3.forceCollide().radius(this.radius))
      // attraction or repulsion between nodes
      .force("charge", d3.forceManyBody())
      // nodes are attracted by the center of the chart area
      .force("center", d3.forceCenter(this.width / 2, this.height / 2))
      // nodes position is updated every tick
      .on("tick", onTick);
  }

  mousemove = (event, data) => {
    this.tooltip
      .html(`<strong>${data.name}</strong> (${data.id})`)
      .style("left", event.pageX + 20 + "px")
      .style("top", event.pageY + "px");
  };

  onZoom = (event) => {
    this.svg.attr("transform", event.transform);
  };
}
