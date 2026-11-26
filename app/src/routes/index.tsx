import { createFileRoute, Link } from "@tanstack/react-router";
import { ArrowDown, ArrowUpRight, Flower2, Menu } from "lucide-react";
import { useState } from "react";
import { ScrollScrub } from "@/components/scroll-scrub/scroll-scrub";
import { scrollScrubScenes, scrollScrubTheme } from "@/scroll-scrub-scenes";

export const Route = createFileRoute("/")({ component: Index });

function Index() {
  const [menuOpen, setMenuOpen] = useState(false);
  return (
    <main className="flore-landing">
      <header className="landing-nav">
        <Link to="/" className="brand-lockup"><span className="brand-mark"><Flower2 size={20}/></span><span>FLORE</span></Link>
        <nav className={menuOpen ? "landing-links open" : "landing-links"}>
          <a href="#filosofia">Filosofía</a><a href="#plataforma">La plataforma</a><a href="#contacto">Contacto</a>
        </nav>
        <Link to="/app" className="landing-enter">Entrar al espacio <ArrowUpRight size={16}/></Link>
        <button className="mobile-menu" onClick={() => setMenuOpen(!menuOpen)} aria-label="Abrir menú"><Menu size={20}/></button>
      </header>
      <div className="landing-intro" id="filosofia">
        <p className="eyebrow">UN SISTEMA PARA FLORISTERÍAS</p>
        <h1>El arte de crear.<br/><em>La claridad de crecer.</em></h1>
        <p>Todo lo que sostiene tu floristería, reunido en un solo lugar.</p>
        <Link to="/app" className="landing-cta">Explorar FLORE <ArrowUpRight size={17}/></Link>
        <span className="intro-note"><ArrowDown size={14}/> UNA NUEVA FORMA DE TRABAJAR</span>
      </div>
      <ScrollScrub scenes={scrollScrubScenes} theme={scrollScrubTheme}/>
      <section className="landing-platform" id="plataforma">
        <div className="platform-copy"><p className="eyebrow">UN SOLO ESPACIO. TODO CONECTADO.</p><h2>Tu negocio también merece florecer.</h2><p>De la primera consulta a la entrega final. De cada tallo a cada peso. FLORE conecta las decisiones diarias con la visión de tu negocio.</p><Link to="/app" className="platform-link">Abrir mi espacio <ArrowUpRight size={16}/></Link></div>
        <div className="platform-grid"><div className="platform-cell"><span>01</span><strong>Operación</strong><p>Pedidos, inventario y tareas en orden.</p></div><div className="platform-cell"><span>02</span><strong>Relaciones</strong><p>Clientes, ocasiones e historial.</p></div><div className="platform-cell"><span>03</span><strong>Rentabilidad</strong><p>Ventas, costos y márgenes visibles.</p></div><div className="platform-cell"><span>04</span><strong>Marca</strong><p>Contenido y señales de tu comunidad.</p></div></div>
      </section>
      <footer className="landing-footer" id="contacto"><Link to="/" className="brand-lockup"><span className="brand-mark"><Flower2 size={18}/></span><span>FLORE</span></Link><span>Un negocio vivo, en equilibrio.</span><Link to="/app">Entrar al espacio <ArrowUpRight size={15}/></Link></footer>
    </main>
  );
}
