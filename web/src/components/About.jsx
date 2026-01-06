import React from 'react';

const About = () => {
    return (
        <section id="about" className="py-20 bg-[var(--color-surface)]/30">
            <div className="container">

                {/* Project Details */}
                <div className="mb-20">
                    <div className="text-center mb-12">
                        <h2 className="text-3xl md:text-4xl font-bold mb-4">About The Project</h2>
                        <div className="h-1 w-20 bg-emerald-500 mx-auto rounded-full"></div>
                    </div>

                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
                        <div className="space-y-6 text-lg text-[var(--color-text-dim)]">
                            <p>
                                The <span className="text-white font-semibold">Smart Voting App</span> is a revolutionary mobile-first solution designed to modernize the electoral process. Built with security and user experience at its core, it leverages Aadhaar-based authentication to ensure that only eligible citizens can cast their votes.
                            </p>
                            <p>
                                Our platform addresses key challenges in traditional voting systems such as accessibility, transparency, and speed. By digitizing the entire workflow—from voter registration to result declaration—we are paving the way for a more robust democratic infrastructure.
                            </p>
                            <ul className="space-y-3 mt-4">
                                <li className="flex items-center gap-3">
                                    <span className="text-emerald-400">✓</span> Secure Aadhaar Verification
                                </li>
                                <li className="flex items-center gap-3">
                                    <span className="text-emerald-400">✓</span> Real-time Vote Processing
                                </li>
                                <li className="flex items-center gap-3">
                                    <span className="text-emerald-400">✓</span> Transparent Result Declaration
                                </li>
                            </ul>
                        </div>

                        {/* Tech Stack or Visual Representation */}
                        <div className="bg-[var(--color-bg)] p-8 rounded-2xl border border-slate-700/50">
                            <h3 className="text-xl font-bold mb-6 text-white">Technical Stack</h3>
                            <div className="grid grid-cols-2 gap-4">
                                <div className="p-4 rounded-xl bg-[var(--color-surface)] border border-slate-700 hover:border-emerald-500/50 transition-colors">
                                    <h4 className="font-semibold text-emerald-400">Android</h4>
                                    <p className="text-sm text-slate-400">Native Java Development</p>
                                </div>
                                <div className="p-4 rounded-xl bg-[var(--color-surface)] border border-slate-700 hover:border-emerald-500/50 transition-colors">
                                    <h4 className="font-semibold text-emerald-400">Firebase</h4>
                                    <p className="text-sm text-slate-400">Real-time Database & Auth</p>
                                </div>
                                <div className="p-4 rounded-xl bg-[var(--color-surface)] border border-slate-700 hover:border-emerald-500/50 transition-colors">
                                    <h4 className="font-semibold text-emerald-400">XML</h4>
                                    <p className="text-sm text-slate-400">Modern Material Design UI</p>
                                </div>
                                <div className="p-4 rounded-xl bg-[var(--color-surface)] border border-slate-700 hover:border-emerald-500/50 transition-colors">
                                    <h4 className="font-semibold text-emerald-400">Security</h4>
                                    <p className="text-sm text-slate-400">Biometric & ID Verification</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Institution & Guide Details */}
                <div className="max-w-4xl mx-auto">
                    <div className="bg-gradient-to-br from-slate-900 to-slate-800 p-8 md:p-12 rounded-3xl border border-slate-700 text-center relative overflow-hidden">
                        <div className="absolute top-0 right-0 p-32 bg-emerald-500/5 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2"></div>

                        <h3 className="text-2xl md:text-3xl font-bold mb-2 relative z-10">Academic Excellence</h3>
                        <p className="text-[var(--color-text-dim)] mb-10 relative z-10">Developed as a Major Project under the guidance of our esteemed faculty.</p>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 relative z-10">
                            <div className="text-center md:text-right md:pr-8 md:border-r border-slate-700">
                                <span className="block text-xs uppercase tracking-wider text-emerald-400 font-bold mb-2">College</span>
                                <h4 className="text-xl font-bold text-white mb-1">[Insert College Name Here]</h4>
                                <p className="text-sm text-slate-400">Department of Computer Science & Engineering</p>
                            </div>

                            <div className="text-center md:text-left md:pl-8">
                                <span className="block text-xs uppercase tracking-wider text-emerald-400 font-bold mb-2">Under The Guidance of</span>
                                <h4 className="text-xl font-bold text-white mb-1">Prof. [Guide Name]</h4>
                                <p className="text-sm text-slate-400">Assistant Professor, Dept. of CSE</p>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
        </section>
    );
};

export default About;
