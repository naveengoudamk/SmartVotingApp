import React from 'react';

const Features = () => {
    const features = [
        {
            title: "Secure Authentication",
            desc: "Aadhaar-based verification ensures only eligible citizens can vote.",
            icon: "🔒"
        },
        {
            title: "Real-time Results",
            desc: "Watch election results unfold live with dynamic visualizations.",
            icon: "📊"
        },
        {
            title: "Multi-Election Support",
            desc: "Participate in multiple elections simultaneously with our batch voting system.",
            icon: "🗳️"
        },
        {
            title: "Dark Mode",
            desc: "A sleek, eye-friendly interface designed for all lighting conditions.",
            icon: "🌙"
        }
    ];

    return (
        <section id="features" className="py-20 bg-[var(--color-surface)]/50">
            <div className="container">
                <div className="text-center mb-16">
                    <h2 className="text-3xl md:text-4xl font-bold mb-4">Why SmartVoting?</h2>
                    <p className="text-[var(--color-text-dim)] max-w-xl mx-auto">
                        Built with modern technology to ensure a fair and efficient voting process.
                    </p>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
                    {features.map((f, i) => (
                        <div key={i} className="p-6 rounded-2xl bg-[var(--color-bg)] border border-slate-700/50 hover:border-emerald-500/50 transition-all duration-300 hover:-translate-y-1">
                            <div className="text-4xl mb-4">{f.icon}</div>
                            <h3 className="text-xl font-bold mb-2">{f.title}</h3>
                            <p className="text-[var(--color-text-dim)] text-sm">{f.desc}</p>
                        </div>
                    ))}
                </div>
            </div>
        </section>
    );
};

export default Features;
