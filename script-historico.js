// Helpers para trabalhar com datas no fuso local (copiado do script-usuario.js)
function getLocalDateString(date = new Date()) {
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

function parseLocalDate(dateStr) {
    if (!dateStr) return new Date();
    const parts = dateStr.split('-').map(Number);
    return new Date(parts[0], parts[1] - 1, parts[2]);
}

// Estado do histórico
let historicoState = {
    habitoId: null,
    historico: {},
    estatisticas: {
        totalDias: 0,
        sequenciaAtual: 0,
        melhorSequencia: 0,
        taxaConclusao: 0
    }
};

// Inicialização
document.addEventListener('DOMContentLoaded', function() {
    const params = new URLSearchParams(window.location.search);
    const habitoId = params.get('habitoId');
    
    if (!habitoId) {
        alert('Hábito não especificado!');
        window.location.href = 'usuario.html';
        return;
    }

    historicoState.habitoId = habitoId;
    carregarHistorico();
});

function carregarHistorico() {
    // Carregar dados do localStorage
    const configuracao = JSON.parse(localStorage.getItem('configuracaoRotinas') || '{}');
    const historico = JSON.parse(localStorage.getItem(`historico_${historicoState.habitoId}`) || '{}');
    
    // Encontrar nome do hábito
    let nomeHabito = 'Hábito não encontrado';
    Object.values(configuracao.dias || {}).forEach(dia => {
        dia.habitos.forEach(h => {
            if (h.id === historicoState.habitoId) {
                nomeHabito = h.nome;
            }
        });
    });

    // Atualizar título
    document.getElementById('habito-titulo').textContent = nomeHabito;

    // Processar dados
    historicoState.historico = historico;
    calcularEstatisticas();
    atualizarEstatisticas();
    gerarGradeContribuicoes();
    atualizarListaDatas();
}

function calcularEstatisticas() {
    const datas = Object.keys(historicoState.historico);
    const hoje = getLocalDateString();

    // Total de dias
    historicoState.estatisticas.totalDias = datas.length;

    // Sequências
    let sequenciaAtual = 0;
    let melhorSequencia = 0;
    let sequenciaTemp = 0;

    datas.sort().forEach((data, index) => {
        if (index === 0) {
            sequenciaTemp = 1;
        } else {
            const dataAtual = parseLocalDate(data);
            const dataAnterior = parseLocalDate(datas[index - 1]);
            const diffDias = Math.floor((dataAtual - dataAnterior) / (1000 * 60 * 60 * 24));

            if (diffDias === 1) {
                sequenciaTemp++;
            } else {
                melhorSequencia = Math.max(melhorSequencia, sequenciaTemp);
                sequenciaTemp = 1;
            }
        }
    });

    // Verificar última sequência
    melhorSequencia = Math.max(melhorSequencia, sequenciaTemp);

    // Verificar se a sequência atual está ativa (último registro é de ontem ou hoje)
    const ultimaData = datas[datas.length - 1];
    if (ultimaData) {
        const diffUltimo = Math.floor((parseLocalDate(hoje) - parseLocalDate(ultimaData)) / (1000 * 60 * 60 * 24));
        sequenciaAtual = (diffUltimo <= 1) ? sequenciaTemp : 0;
    }

    historicoState.estatisticas.sequenciaAtual = sequenciaAtual;
    historicoState.estatisticas.melhorSequencia = melhorSequencia;

    // Taxa de conclusão (últimos 30 dias)
    const diasAtivos = datas.filter(data => {
        const diff = Math.floor((parseLocalDate(hoje) - parseLocalDate(data)) / (1000 * 60 * 60 * 24));
        return diff <= 30;
    }).length;

    historicoState.estatisticas.taxaConclusao = Math.round((diasAtivos / 30) * 100);
}

function atualizarEstatisticas() {
    const { totalDias, sequenciaAtual, melhorSequencia, taxaConclusao } = historicoState.estatisticas;

    document.getElementById('total-dias').textContent = totalDias;
    document.getElementById('sequencia-atual').textContent = sequenciaAtual;
    document.getElementById('melhor-sequencia').textContent = melhorSequencia;
    document.getElementById('taxa-conclusao').textContent = `${taxaConclusao}%`;
}

function gerarGradeContribuicoes() {
    const gridContainer = document.getElementById('contrib-grid');
    gridContainer.innerHTML = '';

    // Gerar datas para o último ano
    const hoje = new Date();
    const inicioAno = new Date(hoje);
    inicioAno.setFullYear(hoje.getFullYear() - 1);

    // Criar grid de 52 semanas
    for (let i = 0; i < 365; i++) {
        const data = new Date(inicioAno);
        data.setDate(data.getDate() + i);
        const dataStr = getLocalDateString(data);
        
        const div = document.createElement('div');
        div.className = 'contrib-day';
        
        // Determinar nível de contribuição (0-4)
        let level = 0;
        if (historicoState.historico[dataStr]) {
            const contagem = historicoState.historico[dataStr];
            if (contagem >= 4) level = 4;
            else level = contagem;
        }
        
        div.setAttribute('data-level', level);
        div.title = `${dataStr}: ${level} atividade(s)`;
        
        gridContainer.appendChild(div);
    }
}

function atualizarListaDatas() {
    const container = document.getElementById('lista-datas');
    const datas = Object.keys(historicoState.historico).sort().reverse();
    
    container.innerHTML = datas.map(data => {
        const contagem = historicoState.historico[data];
        return `
            <div class="item-data">
                <i class="fas fa-check"></i>
                ${formatarData(data)} - ${contagem} atividade(s)
            </div>
        `;
    }).join('');
}

function formatarData(dataStr) {
    const data = parseLocalDate(dataStr);
    return data.toLocaleDateString('pt-BR', { 
        weekday: 'long', 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric' 
    });
}