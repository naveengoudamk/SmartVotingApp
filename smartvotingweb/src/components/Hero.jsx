import React from 'react';

const Hero = () => {
  return (
    <section className="relative pt-32 pb-20 overflow-hidden">
      <div className="container relative z-10 text-center">
        <div className="inline-block px-4 py-1.5 mb-6 text-sm font-medium text-emerald-400 bg-emerald-900/30 rounded-full animate-fade-in" style={{ animationDelay: '0.1s' }}>
          ✨ Version 2.0 Now Available
        </div>
        
        <h1 className="text-5xl md:text-7xl font-bold mb-6 tracking-tight animate-fade-in" style={{ animationDelay: '0.2s' }}>
          Smart<span className="text-gradient">Voting</span>
        </h1>
        
        <p className="text-lg md:text-xl text-[var(--color-text-dim)] max-w-2xl mx-auto mb-10 animate-fade-in" style={{ animationDelay: '0.3s' }}>
          Experience the next generation of digital democracy. Secure, transparent, and built for the future of India.
        </p>
        
        <div className="flex flex-col sm:flex-row gap-4 justify-center items-center animate-fade-in" style={{ animationDelay: '0.4s' }}>
          <a href="/SmartVotingApp.apk" download className="btn group">
            <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
            </svg>
            Download APK
          </a>
          <a href="#features" className="px-6 py-3 text-sm font-medium hover:text-white transition-colors">
            Learn More →
          </a>
        </div>
      </div>
      
      {/* Background Elements */}
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[800px] h-[400px] bg-emerald-500/10 blur-[100px] rounded-full -z-10 pointer-events-none" />
    </section>
  );
};

export default Hero;
