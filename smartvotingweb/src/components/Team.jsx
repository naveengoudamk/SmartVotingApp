import React from 'react';

const TeamMember = ({ name, role, isLead }) => (
    <div className={`relative group ${isLead ? 'md:col-span-2 lg:col-span-1 border-emerald-500/50' : 'border-slate-700/50'}`}>
        <div className="h-full bg-[var(--color-bg)] p-6 rounded-2xl border transition-all duration-300 hover:-translate-y-2 hover:border-emerald-500 hover:shadow-xl hover:shadow-emerald-500/10">
            <div className={`w-24 h-24 mx-auto mb-6 rounded-full flex items-center justify-center text-3xl shadow-lg ${isLead ? 'bg-gradient-to-br from-emerald-400 to-cyan-500' : 'bg-slate-800'}`}>
                {isLead ? '👨‍💻' : '👤'}
            </div>

            <h3 className="text-xl font-bold mb-1">{name}</h3>
            <p className={`text-sm mb-4 ${isLead ? 'text-emerald-400 font-medium' : 'text-slate-400'}`}>{role}</p>

            <div className="flex justify-center gap-4 opacity-60 group-hover:opacity-100 transition-opacity">
                <a href="#" className="hover:text-emerald-400 transition-colors" title="GitHub">
                    <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24"><path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z" /></svg>
                </a>
                <a href="#" className="hover:text-emerald-400 transition-colors" title="LinkedIn">
                    <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24"><path d="M19 0h-14c-2.761 0-5 2.239-5 5v14c0 2.761 2.239 5 5 5h14c2.762 0 5-2.239 5-5v-14c0-2.761-2.238-5-5-5zm-11 19h-3v-11h3v11zm-1.5-12.268c-.966 0-1.75-.79-1.75-1.764s.784-1.764 1.75-1.764 1.75.79 1.75 1.764-.783 1.764-1.75 1.764zm13.5 12.268h-3v-5.604c0-3.368-4-3.113-4 0v5.604h-3v-11h3v1.765c1.396-2.586 7-2.777 7 2.476v6.759z" /></svg>
                </a>
            </div>
        </div>
    </div>
);

const Team = () => {
    return (
        <section className="py-20">
            <div className="container text-center">

                <div className="mb-16">
                    <h2 className="text-3xl md:text-4xl font-bold mb-4">Meet The Team</h2>
                    <p className="text-[var(--color-text-dim)] max-w-xl mx-auto">
                        The brilliant minds behind this project, dedicated to building the future of voting.
                    </p>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 max-w-6xl mx-auto">
                    {/* Main Profile */}
                    <TeamMember
                        name="Naveengouda M K"
                        role="Project Lead & Developer"
                        isLead={true}
                    />

                    {/* Team Members */}
                    <TeamMember
                        name="[Team Member Name]"
                        role="Full Stack Developer"
                        isLead={false}
                    />
                    <TeamMember
                        name="[Team Member Name]"
                        role="UI/UX Designer"
                        isLead={false}
                    />
                    <TeamMember
                        name="[Team Member Name]"
                        role="Database Admin"
                        isLead={false}
                    />
                </div>

            </div>
        </section>
    );
};

export default Team;
