import React from 'react';

const About = () => {
    return (
        <section id="about" className="py-24 bg-[var(--color-surface)]/30 relative overflow-hidden">
            {/* Decorative Background Elements */}
            <div className="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none">
                <div className="absolute top-20 left-1/2 -translate-x-1/2 w-[800px] h-[800px] bg-emerald-500/5 rounded-full blur-3xl"></div>
            </div>

            <div className="container relative z-10">

                {/* Header - Strictly Centered */}
                <div className="text-center max-w-6xl mx-auto mb-16">
                    <h2 className="text-4xl md:text-5xl font-bold mb-6 tracking-tight">About The Project</h2>
                    <div className="h-1.5 w-24 bg-gradient-to-r from-emerald-400 to-blue-500 mx-auto rounded-full mb-8"></div>

                    <div className="text-lg md:text-xl text-[var(--color-text-dim)] leading-relaxed space-y-6">
                        <p>
                            The <span className="text-white font-semibold">Smart Voting App</span> is a revolutionary mobile-first solution designed to modernize the electoral process.
                            Built with security and user experience at its core, it leverages Aadhaar-based authentication to ensure that only eligible citizens can cast their votes.
                        </p>
                        <p>
                            By digitizing the entire workflow—from voter registration to result declaration—we are paving the way for a more robust, transparent, and efficient democratic infrastructure.
                        </p>
                    </div>

                    {/* Key Features Pills - Centered */}
                    <div className="flex flex-wrap gap-3 justify-center mt-8">
                        <span className="px-5 py-2.5 bg-emerald-500/10 text-emerald-400 rounded-full text-sm font-medium border border-emerald-500/20 shadow-sm shadow-emerald-500/5">
                            🔒 Secure Aadhaar Verification
                        </span>
                        <span className="px-5 py-2.5 bg-blue-500/10 text-blue-400 rounded-full text-sm font-medium border border-blue-500/20 shadow-sm shadow-blue-500/5">
                            ⚡ Real-time Processing
                        </span>
                        <span className="px-5 py-2.5 bg-purple-500/10 text-purple-400 rounded-full text-sm font-medium border border-purple-500/20 shadow-sm shadow-purple-500/5">
                            📊 Live Analytics
                        </span>
                    </div>
                </div>

                {/* Project Resources - Centered Row */}
                <div className="max-w-5xl mx-auto mb-20">
                    <div className="bg-gradient-to-b from-[var(--color-bg)] to-[var(--color-surface)] p-1 rounded-3xl border border-slate-700/50 shadow-2xl">
                        <div className="bg-[var(--color-bg)] rounded-[22px] p-8 md:p-12 text-center">
                            <h3 className="text-2xl font-bold mb-8 flex items-center justify-center gap-3">
                                <span>📁</span> Project Documentation
                            </h3>

                            <div className="flex flex-col sm:flex-row gap-6 justify-center">
                                <a href="/Project_Report.pdf" download className="group flex-1 max-w-sm mx-auto flex items-center p-4 rounded-xl border border-slate-700 hover:border-emerald-500 bg-slate-800/50 hover:bg-slate-800 transition-all duration-300">
                                    <div className="w-14 h-14 rounded-full bg-slate-700 group-hover:bg-emerald-500/20 text-3xl flex items-center justify-center transition-colors">
                                        📄
                                    </div>
                                    <div className="ml-5 text-left flex-1">
                                        <h4 className="font-bold text-white group-hover:text-emerald-400 transition-colors">Project Report</h4>
                                        <p className="text-xs text-slate-400">Download PDF File</p>
                                    </div>
                                    <div className="text-slate-500 group-hover:text-emerald-400 transform group-hover:translate-x-1 transition-all pr-2">
                                        ⬇
                                    </div>
                                </a>

                                <a href="/Project_Presentation.pptx" download className="group flex-1 max-w-sm mx-auto flex items-center p-4 rounded-xl border border-slate-700 hover:border-blue-500 bg-slate-800/50 hover:bg-slate-800 transition-all duration-300">
                                    <div className="w-14 h-14 rounded-full bg-slate-700 group-hover:bg-blue-500/20 text-3xl flex items-center justify-center transition-colors">
                                        📽️
                                    </div>
                                    <div className="ml-5 text-left flex-1">
                                        <h4 className="font-bold text-white group-hover:text-blue-400 transition-colors">Presentation</h4>
                                        <p className="text-xs text-slate-400">Download PPT File</p>
                                    </div>
                                    <div className="text-slate-500 group-hover:text-blue-400 transform group-hover:translate-x-1 transition-all pr-2">
                                        ⬇
                                    </div>
                                </a>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Academic Guidance - Centered Grid */}
                <div className="max-w-6xl mx-auto text-center">
                    <h3 className="text-sm font-bold uppercase tracking-widest text-emerald-500 mb-8 opacity-80">Under The Guidance Of</h3>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div className="group p-8 rounded-2xl bg-[var(--color-bg)] border border-slate-800 hover:border-emerald-500/30 transition-all duration-300 hover:-translate-y-1 hover:shadow-lg hover:shadow-emerald-500/5">
                            <div className="text-5xl mb-6 transform group-hover:scale-110 transition-transform duration-300">🏫</div>
                            <h4 className="text-xl font-bold text-white mb-2 group-hover:text-emerald-400 transition-colors">[Insert College Name]</h4>
                            <p className="text-sm text-slate-400">Department of Computer Science & Engineering</p>
                        </div>

                        <div className="group p-8 rounded-2xl bg-[var(--color-bg)] border border-slate-800 hover:border-emerald-500/30 transition-all duration-300 hover:-translate-y-1 hover:shadow-lg hover:shadow-emerald-500/5">
                            <div className="text-5xl mb-6 transform group-hover:scale-110 transition-transform duration-300">👨‍🏫</div>
                            <h4 className="text-xl font-bold text-white mb-2 group-hover:text-emerald-400 transition-colors">Prof. [Guide Name]</h4>
                            <p className="text-sm text-slate-400">Project Guide & Mentor</p>
                        </div>
                    </div>
                </div>

            </div>
        </section>
    );
};

export default About;
