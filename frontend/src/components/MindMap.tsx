import React, { useEffect, useRef } from "react";

interface MindMapNode {
  title: string;
  children?: MindMapNode[];
}

interface MindMapProps {
  data: MindMapNode;
}

/**
 * 简易思维导图渲染组件
 * 使用 Canvas 绘制树形结构（不依赖外部库）
 */
const MindMap: React.FC<MindMapProps> = ({ data }) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    // 清空
    canvas.width = canvas.offsetWidth * 2;
    canvas.height = canvas.offsetHeight * 2;
    ctx.scale(2, 2);
    ctx.clearRect(0, 0, canvas.offsetWidth, canvas.offsetHeight);

    const width = canvas.offsetWidth;
    const height = canvas.offsetHeight;

    // 递归绘制节点
    const drawNode = (node: MindMapNode, x: number, y: number, level: number, maxWidth: number) => {
      const boxW = Math.min(maxWidth, 120);
      const boxH = 30;
      const colors = ["#1890ff", "#52c41a", "#faad14", "#f5222d", "#722ed1"];
      const color = colors[level % colors.length];

      // 绘制节点框
      ctx.fillStyle = color;
      ctx.strokeStyle = color;
      ctx.lineWidth = 1;
      const rx = x - boxW / 2;
      ctx.beginPath();
      ctx.roundRect(rx, y - boxH / 2, boxW, boxH, 6);
      ctx.fill();

      // 绘制文字
      ctx.fillStyle = "#fff";
      ctx.font = level === 0 ? "bold 14px sans-serif" : "12px sans-serif";
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      const displayText = node.title.length > 10 ? node.title.slice(0, 10) + "..." : node.title;
      ctx.fillText(displayText, x, y);

      // 绘制子节点
      if (node.children && node.children.length > 0) {
        const childCount = node.children.length;
        const totalWidth = Math.min(maxWidth * 0.8, width - 100);
        const gap = totalWidth / childCount;
        const childX_start = x - totalWidth / 2 + gap / 2;
        const childY = y + 60 + level * 10;

        node.children.forEach((child, i) => {
          const childX = childX_start + i * gap;

          // 连线
          ctx.strokeStyle = "#d9d9d9";
          ctx.lineWidth = 1.5;
          ctx.beginPath();
          ctx.moveTo(x, y + boxH / 2);
          ctx.quadraticCurveTo(x, childY - 20, childX, childY - boxH / 2);
          ctx.stroke();

          drawNode(child, childX, childY, level + 1, gap * 0.9);
        });
      }
    };

    drawNode(data, width / 2, 40, 0, width * 0.8);
  }, [data]);

  return (
    <canvas
      ref={canvasRef}
      style={{ width: "100%", height: 500, border: "1px solid #f0f0f0", borderRadius: 8 }}
    />
  );
};

export default MindMap;
