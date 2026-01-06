import React from 'react';
import Hero from './components/Hero';
import Features from './components/Features';
import Team from './components/Team';
import Footer from './components/Footer';

import About from './components/About';

function App() {
  return (
    <main className="min-h-screen">
      <Hero />
      <Features />
      <About />
      <Team />
      <Footer />
    </main>
  );
}

export default App;
