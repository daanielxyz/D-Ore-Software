import type { ScrollScrubScene, ScrollScrubTheme } from "@/components/scroll-scrub/scroll-scrub";

export const scrollScrubTheme: ScrollScrubTheme = {
  accent: "#B7D5C3",
  background: "#102A25",
  ink: "#F2F4EE",
  muted: "#D0DCD3",
};

export const scrollScrubScenes: ScrollScrubScene[] = [
  {
    id: "materia-viva",
    label: "FLORE",
    poster: "/assets/world/flore-poster.jpg",
    mobilePoster: "/assets/world/flore-poster-mobile.jpg",
    clip: "/assets/world/flore-film.mp4",
    mobileClip: "/assets/world/flore-film-mobile.mp4",
    title: "Todo empieza con una flor.",
    body: "La belleza nace en el detalle. El negocio también merece ese mismo cuidado. Pedidos, inventario, clientes y rentabilidad conectados en un solo espacio.",
    kicker: "FLORE · FLORAL BUSINESS OS",
    tags: ["Operación", "Relaciones", "Rentabilidad"],
    scroll: 2.8,
  },
];
